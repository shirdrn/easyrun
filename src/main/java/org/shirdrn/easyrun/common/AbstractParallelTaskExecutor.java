package org.shirdrn.easyrun.common;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.config.Configuration;
import org.shirdrn.easyrun.common.config.ContextReadable;
import org.shirdrn.easyrun.component.common.ThreadPoolFactory;
import org.shirdrn.easyrun.component.common.ThreadPoolService;
import org.shirdrn.easyrun.utils.FactoryUtils;


public abstract class AbstractParallelTaskExecutor<E> extends AbstractIterableTaskExecutor<E> {

	private static final Log LOG = LogFactory.getLog(AbstractParallelTaskExecutor.class);
	private ObjectFactory<ContextReadable, ThreadPoolService> threadPoolFactory;
	private final Object lock = new Object();
	
	private long checkInterval = 500;
	private BlockingQueue<Future<ChildTaskExecutionResult>> futureQ;
	private ChildTaskExecutor errorChildTaskExecutor;
	private volatile boolean completed = false;
	private final AtomicBoolean notified = new AtomicBoolean(false);
	private Configuration config;
	private boolean isThreadPoolShared = true;
	
	public AbstractParallelTaskExecutor() {
		super();
	}
	
	@Override
	public void configure(Configuration config) {
		super.configure(config);
		this.config = config;
		checkInterval = config.getRContext().getLong("component.thread.pool.future.queue.check.interval", 500);
		
		String poolFactoryClazz = config.getRContext().get("component.thread.pool.factory.class", 
				"org.shirdrn.easyrun.component.threadpool.DefaultThreadPoolFactory");
		threadPoolFactory = (ObjectFactory<ContextReadable, ThreadPoolService>) FactoryUtils.getFactory(poolFactoryClazz, ThreadPoolFactory.class);
		isThreadPoolShared = config.getRContext().getBoolean("component.thread.pool.shared", true);
		int futureQSize = config.getRContext().getInt("component.thread.pool.future.queue.size", Integer.MAX_VALUE);
		futureQ = new LinkedBlockingQueue<Future<ChildTaskExecutionResult>>(futureQSize);
	}
	
	protected ThreadPoolService getThreadPool() {
		return threadPoolFactory.get(config.getRContext());
	}

	@Override
	public void doBody() throws Exception {
		// initialize future checker
		LOG.info("Try to start checker thread...");
		final ResultChecker checker = new ResultChecker();
		checker.setName("CHECKER");
		checker.setDaemon(true);
		checker.start();
		LOG.info("Checker started!");
		// run body
		super.doBody();
		completed = true;
		LOG.info("Set completed flag: completed=" + completed);
		// wait child task executor to finish executing
		synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				LOG.info("Received notification, then continue...");
			}
		}
		// close thread pool
		if(!isThreadPoolShared) {
			LOG.info("Close thread pool: pool=" + getThreadPool());
			threadPoolFactory.close(getThreadPool());
		}
		LOG.info("Parent exit.");
	}
	
	@Override
	protected ChildTaskExecutor getErrorChildTaskExecutor() {
		return errorChildTaskExecutor;
	}

	private static final Log KOG = LogFactory.getLog(AbstractParallelTaskExecutor.ResultChecker.class);
	
	final class ResultChecker extends Thread {
		
		private boolean running = false;
		
		public ResultChecker() {
			super();
			this.running = true;
		}
		
		@Override
		public void run() {
			while(running) {
				KOG.debug("Check counter: counter=" + counter.get() + ", totalCount=" + totalCount.get());
				try {
					Future<ChildTaskExecutionResult> f = futureQ.poll();
					if(f == null) {
						KOG.info("Future queue is empty, Wait...");
						Thread.sleep(checkInterval);
					} else {
						KOG.debug("Check child task: future=" + f + ", qsize=" + futureQ.size());
						ChildTaskExecutionResult result = f.get();
						// no child error
						KOG.debug("Before check result.");
						if(!childCaughtError) {
							if(result.getStatus() == Status.SUCCESS) {
								KOG.info(logResult(result));
							} else if(result.getStatus() == Status.FAILURE) {
								childCaughtError = true;
								KOG.info(logResult(result));
								executionResult.getFailureCauses().addAll(result.getFailureCauses());
								errorChildTaskExecutor = result.getChildTaskExecutor();
								// cancel all submitted already running tasks
								if(isTerminateWhenFailure()) {
									cancelAll();
									break;
								}
							}
						} else {
							if(result.getStatus() == Status.SUCCESS) {
								KOG.info(logResult(result));
							} else {
								if(!isTerminateWhenFailure()) {
									KOG.warn("Ignore. " + logResult(result));
								} else {
									KOG.error("Ignore. " + logResult(result));
								}
							}
						}
						KOG.debug("After check result.");
					}
				} catch (Exception e) {
					KOG.warn("Check future queue, catch exeception: ", e);
				}
				
				KOG.debug("Check: completed=" + completed + ", isFutureQEmpty=" + futureQ.isEmpty());
				if(completed) {
					if(futureQ.isEmpty()) {
						notifyParent();
						running = false;
						KOG.info("Checker prepare to exit...");
					}
				}
			}
			// finally, set parent executor status
			if(childCaughtError) {
				while(!completed) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { }
				}
				notifyParent();
			}
			KOG.info("Finished to run checker thread.");
		}
		
		private String logResult(ChildTaskExecutionResult result) {
			return "Child result: " + result.toString();
		}
		
		private void cancelAll() {
			Iterator<Future<ChildTaskExecutionResult>> iter = futureQ.iterator();
			while(iter.hasNext()) {
				Future<ChildTaskExecutionResult> f = iter.next();
				try {
					f.cancel(true);
					KOG.info("Attempt to cancel child task: name=" + f.get().getName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void notifyParent() {
		if(notified.compareAndSet(false, true)) {
			LOG.info("Notify parent to exit.");
			synchronized(lock) {
				lock.notify();
			}
		}
	}
	
	@Override
	protected void startChildTaskExecutor(E element) throws ChildTaskExecutionException {
		Worker worker = new Worker(element);
		if(!childCaughtError 
				|| (childCaughtError && !isTerminateWhenFailure())) {
			Future<ChildTaskExecutionResult> future = getThreadPool().submit(worker);
			LOG.debug("Submit a child task: worker=" + worker);
			try {
				futureQ.put(future);
				LOG.debug("Put a future to futureQ: " + future);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			LOG.warn("Error caught, discard child task: " + worker);
		}
	}

	class Worker extends ChildTaskExecutor implements Callable<ChildTaskExecutionResult> {

		public Worker(E element) {
			super(element);
		}

		@Override
		public ChildTaskExecutionResult call() throws Exception {
			try {
				super.execute();
			} catch (Exception e) {
				childResult.getFailureCauses().add(e);
			}
			return childResult;
		}
	}
	
}
