package org.shirdrn.easyrun.common;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.component.threadpool.ThreadPoolFactory;
import org.shirdrn.easyrun.utils.FactoryUtils;


public abstract class AbstractParallelTaskExecutor extends AbstractIterableTaskExecutor {

	private static final Log LOG = LogFactory.getLog(AbstractParallelTaskExecutor.class);
	private ObjectFactory<ContextReadable, ThreadPoolService> threadPoolFactory;
	private final Object lock = new Object();
	
	private long checkInterval = 500;
	private BlockingQueue<Future<ChildTaskExecutionResult>> futureQ;
	private ChildTaskExecutor errorChildExecutor;
	private volatile boolean completed = false;
	private final AtomicBoolean notified = new AtomicBoolean(false);
	private Configuration config;
	
	public AbstractParallelTaskExecutor() {
		super();
	}
	
	@Override
	public void configure(Configuration config) {
		super.configure(config);
		this.config = config;
		checkInterval = config.getRContext().getLong("component.thread.pool.future.queue.check.interval", 500);
		threadPoolFactory = FactoryUtils.getFactory(ThreadPoolFactory.class);
		int futureQSize = config.getRContext().getInt("component.thread.pool.future.queue.size", Integer.MAX_VALUE);
		futureQ = new LinkedBlockingQueue<Future<ChildTaskExecutionResult>>(futureQSize);
	}
	
	protected ExecutorService getThreadPool() {
		return threadPoolFactory.get(config.getRContext());
	}

	@Override
	public void doBody() {
		// initialize future checker
		final ChildExecutionChecker checker = new ChildExecutionChecker();
		checker.setName("CHECKER");
		checker.start();
		// run body
		super.doBody();
		LOG.info("Set completed to true!");
		completed = true;
		// wait child SQL executor to finish executing
		synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
	@Override
	protected ChildTaskExecutor getErrorChildTaskExecutor() {
		return errorChildExecutor;
	}

	private static final Log CKLOG = LogFactory.getLog(ChildExecutionChecker.class);
	final class ChildExecutionChecker extends Thread {
		@Override
		public void run() {
			while(!completed) {
				CKLOG.debug("Check counter: counter=" + counter.get() + ", totalCount=" + totalCount.get());
				try {
					CKLOG.debug("Check future queue: size=" + futureQ.size());
					Iterator<Future<ChildTaskExecutionResult>> iter = futureQ.iterator();
					while(iter.hasNext()) {
						Future<ChildTaskExecutionResult> f = iter.next();
						Status status = f.get().getStatus();
						if(status == Status.SUCCESS) {
							iter.remove();
						} else if(status == Status.FAILURE) {
							CKLOG.info("Child SQL executor failure: childResult=" + f.get());
							executionResult.setCause(f.get().getCause());
							errorChildExecutor = f.get().getChildTaskExecutor();
							childCaughtError = true;
							// cancel all submitted already running tasks
							if(isTerminateWhenChildFailure()) {
								completed = true;
								cancelAll();
							}
							CKLOG.info("Child error: completed=" + completed + ", childCaughtError=" + childCaughtError + ",");
							break;
						}
					}
				} catch (Exception e) {
					CKLOG.warn("Check future queue: ", e);
				} finally {
					try {
						Thread.sleep(checkInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void cancelAll() {
			Iterator<Future<ChildTaskExecutionResult>> iter = futureQ.iterator();
			while(iter.hasNext()) {
				Future<ChildTaskExecutionResult> f = iter.next();
				boolean cancelled = f.cancel(true);
				try {
					CKLOG.info("Cancel child task: name=" + f.get().getChildTaskExecutor().getClass().getSimpleName() + ", id=" + f.get().getId() + ", cancelled=" + cancelled);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void fireChildSQLs(String[] childSqls) throws ChildTaskExecutionException {
		InsertionWorker worker = new InsertionWorker(childSqls);
		Future<ChildTaskExecutionResult> future = getThreadPool().submit(worker);
		LOG.debug("Submit a child task: worker=" + worker);
		try {
			futureQ.put(future);
			LOG.debug("Put a future to futureQ: " + future);
		} catch (InterruptedException e) {
		}
	}
	
	
	private static final Log IWLOG = LogFactory.getLog(InsertionWorker.class);
	class InsertionWorker extends ChildTaskExecutor implements Callable<ChildTaskExecutionResult> {

		public InsertionWorker(String[] sqls) {
			super(sqls);
		}

		@Override
		public ChildTaskExecutionResult call() throws Exception {
			try {
				super.execute();
			} catch (Exception e) {
				if(!(e instanceof InterruptedException)) {
					if(childResult.getCause() == null) {
						childResult.setCause(e);
					}
				} else {
					IWLOG.info("Interrupted, and exit.");
				}
			}
			return childResult;
		}
		
		@Override
		protected void afterRun() {
			super.afterRun();
			IWLOG.debug("Counter: counter=" + counter.get() + ", totalCount=" + totalCount.get() + ", completed=" + completed);
			if(completed) {
				// abnormally exit
				if(childCaughtError && isTerminateWhenChildFailure()) {
					notifyParent();
					return;
				}
				// permit to execute regardless of child errors
				if(counter.get() == totalCount.get()) {
					notifyParent();
				}
			}
		}


	}
	
	private void notifyParent() {
		if(notified.compareAndSet(false, true)) {
			LOG.info("Notify parent.");
			synchronized(lock) {
				lock.notify();
			}
		}
	}

}
