package org.shirdrn.easyrun.component.threadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * If a task is submitted to the thread pool, but there's no chance to
 * execute the task, we should wait for a moment and submitted it again.
 * 
 * @author Shirdrn
 *
 */
public class ScheduleAgainPolicy implements RejectedExecutionHandler {

	private static final Log LOG = LogFactory.getLog(ScheduleAgainPolicy.class);
	private int checkIdleWorkerInterval = 1000;
	private int workQueueSize = 1;

	/** Creates a <tt>ScheduleAgainPolicy</tt> instance. */
    public ScheduleAgainPolicy(int workQueueSize) {
    	super();
    	this.workQueueSize = workQueueSize;
    }

    /**
     * If no worker thread is idle, caller is entering loop until a worker thread is released.
     * The idle worker thread executes task r out of the caller's thread, unless the executor
     * has been shut down, in which case the task is discarded.
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		while(!executor.isShutdown() && executor.getQueue().size()>=workQueueSize) {
    		try {
				Thread.sleep(checkIdleWorkerInterval);
				LOG.debug("Loop statistics;" + "sleepInterval=" + checkIdleWorkerInterval + "," +
						"poolSize=" + executor.getPoolSize() + "," + "activeCount=" + executor.getActiveCount() + "," +
						"queueSize=" + executor.getQueue().size() + "," + "completedTaskCount=" + executor.getCompletedTaskCount() + "," +
						"taskCount=" + executor.getTaskCount() + "," + "largestPoolSize=" + executor.getLargestPoolSize()
						);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
		// Push back to thread pool, 
		// and re-schedule worker thread to process task r.
		executor.execute(r);			
	}
}
