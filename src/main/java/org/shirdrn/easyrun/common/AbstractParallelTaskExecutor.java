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
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.config.ContextReadable;
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
	
	public AbstractParallelTaskExecutor() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure(Configuration config) {
		super.configure(config);
		this.config = config;
		checkInterval = config.getRContext().getLong("component.thread.pool.future.queue.check.interval", 500);
		String poolClazz = config.getRContext().get("component.thread.pool.class", "org.shirdrn.easyrun.component.threadpool.ThreadPoolFactory");
		threadPoolFactory = (ObjectFactory<ContextReadable, ThreadPoolService>) FactoryUtils.getFactory(poolClazz);
		int futureQSize = config.getRContext().getInt("component.thread.pool.future.queue.size", Integer.MAX_VALUE);
		if(futureQSize != Integer.MAX_VALUE) {
			futureQ = new LinkedBlockingQueue<Future<ChildTaskExecutionResult>>(futureQSize);
		} else {
			futureQ = new LinkedBlockingQueue<Future<ChildTaskExecutionResult>>();
		}
	}
	
	protected ThreadPoolService getThreadPool() {
		return threadPoolFactory.get(config.getRContext());
	}

	@Override
	public void doBody() throws Exception {
		// initialize future checker
		LOG.info("Try to start child execution checker thread...");
		final ChildExecutionChecker checker = new ChildExecutionChecker();
		checker.setName("CHECKER");
		checker.start();
		LOG.info("Child execution checker started!");
		// run body
		super.doBody();
		completed = true;
		LOG.info("Set completed flag: completed=" + completed);
		// wait child task executor to finish executing
		synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			} finally {
				LOG.info("Received notification, and continue...");
			}
		}
		// close thread pool
		LOG.info("Close thread pool: pool=" + getThreadPool());
		threadPoolFactory.close(getThreadPool());
		LOG.info("Parent exit.");
	}
	
	@Override
	protected ChildTaskExecutor getErrorChildTaskExecutor() {
		return errorChildTaskExecutor;
	}

	private final Log CKLOG = LogFactory.getLog(ChildExecutionChecker.class);
	final class ChildExecutionChecker extends Thread {
		private boolean running = false;
		public ChildExecutionChecker() {
			this.running = true;
		}
		@Override
		public void run() {
			while(running) {
				CKLOG.debug("Check counter: counter=" + counter.get() + ", totalCount=" + totalCount.get());
				try {
					if(!futureQ.isEmpty()) {
						CKLOG.debug("Check future queue: size=" + futureQ.size());
						Iterator<Future<ChildTaskExecutionResult>> iter = futureQ.iterator();
						while(iter.hasNext()) {
							Future<ChildTaskExecutionResult> f = iter.next();
							// no child error
							if(!childCaughtError) {
								if(f.get().getStatus() == Status.SUCCESS) {
									iter.remove();
									CKLOG.debug("Removed: result=" + f.get());
								} else if(f.get().getStatus() == Status.FAILURE) {
									childCaughtError = true;
									CKLOG.info("Child task executor failure: childResult=" + f.get());
									executionResult.setFailureCause(f.get().getFailureCause());
									errorChildTaskExecutor = f.get().getChildTaskExecutor();
									// cancel all submitted already running tasks
									if(terminateWhenFailure) {
										cancelAll();
										break;
									}
								}
							} else {
								if(!terminateWhenFailure) {
									if(f.get().getStatus() == Status.SUCCESS) {
										CKLOG.info("Child task executor success: childResult=" + f.get());
									} else {
										CKLOG.warn("Child task executor success: childResult=" + f.get());
									}
									iter.remove();
								}
							}
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
				
				// error caught
				if(childCaughtError && completed && terminateWhenFailure) {
					notifyParent();
					CKLOG.info("Child task execution checker prepare to exit...");
					running = false;
				}
			}
			CKLOG.info("Finished to run child execution checker thread.");
		}

		private void cancelAll() {
			Iterator<Future<ChildTaskExecutionResult>> iter = futureQ.iterator();
			while(iter.hasNext()) {
				Future<ChildTaskExecutionResult> f = iter.next();
				try {
					f.cancel(true);
					CKLOG.info("Attempt to cancel child task: id=" + f.get().getId() + ", name=" + f.get().getChildTaskExecutor().getClass().getSimpleName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void startChildTaskExecutor(E element) throws ChildTaskExecutionException {
		Worker worker = new Worker(element);
		if(!childCaughtError 
				|| (childCaughtError && !terminateWhenFailure)) {
			Future<ChildTaskExecutionResult> future = getThreadPool().submit(worker);
			LOG.info("Submit a child task: worker=" + worker);
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
				if(!(e instanceof InterruptedException)) {
					if(childResult.getFailureCause() == null) {
						childResult.setFailureCause(e);
					}
				} else {
					LOG.info("Interrupted, and exit.");
				}
			}
			return childResult;
		}
		
		@Override
		protected void afterRun() {
			super.afterRun();
			LOG.debug("Counter: counter=" + counter.get() + ", totalCount=" + totalCount.get() + ", completed=" + completed);
			if(completed) {
				// permit to execute regardless of child errors
				if(counter.get() == totalCount.get()) {
					notifyParent();
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

}
