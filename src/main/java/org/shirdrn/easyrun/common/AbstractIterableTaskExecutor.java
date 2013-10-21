package org.shirdrn.easyrun.common;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractIterableTaskExecutor<E> extends AbstractDefaultTaskExecutor implements Iterable<E> {

	private static final Log LOG = LogFactory.getLog(AbstractIterableTaskExecutor.class);
	protected final AtomicInteger totalCount = new AtomicInteger(0);
	protected final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicInteger workerIdCounter = new AtomicInteger(0);
	protected volatile boolean childCaughtError = false;
	
	public AbstractIterableTaskExecutor() {
		super();
	}
	
	@Override
	public void configure(Configuration config) {
		super.configure(config);
	}
	
	protected abstract TaskExecutor<ChildTaskExecutionResult> getErrorChildTaskExecutor();
	
	@Override
	public void doBody() throws Exception {
		Iterator<E> iter = iterator();
		try {
			while(iter.hasNext()) {
				try {
					E element = iter.next();
					if(childCaughtError && isTerminateWhenFailure()) {
						LOG.debug("childCaughtError=" + childCaughtError);
						TaskExecutor<ChildTaskExecutionResult> child = getErrorChildTaskExecutor();
						LOG.info("Child failed to execute: childResult=" + child.getResult());
						throw new ChildTaskExecutionException(child.getResult());
					}
					startChildTaskExecutor(element);
					totalCount.incrementAndGet();
				} catch (Exception e) {
					throw e;
				}
			}
		} catch (Exception e) {
			executionResult.getFailureCauses().add(e);
			throw e;
		}
	}
	
	protected abstract void startChildTaskExecutor(E element) throws ChildTaskExecutionException;
	
	/**
	 * Process a element in child task executor.
	 * @param element
	 * @throws Exception
	 */
	protected abstract void process(E element) throws Exception;
	
	private static final Log COG = LogFactory.getLog(AbstractIterableTaskExecutor.ChildTaskExecutor.class);
	
	class ChildTaskExecutor implements TaskExecutor<ChildTaskExecutionResult> {
		
		private int id;
		protected final E element;
		protected final ChildTaskExecutionResult childResult;
		protected int maxChildRetryTimes = 0;
		private Date startTime;
		private Date finishTime;
		
		public ChildTaskExecutor(E element) {
			super();
			this.element = element;
			childResult = new ChildTaskExecutionResult();
			childResult.setChildTaskExecutor(this);
			id = workerIdCounter.incrementAndGet();
			childResult.setName(String.valueOf(id));
		}
		
		@Override
		public void configure(Configuration config) {
			maxChildRetryTimes = config.getRContext().getInt("common.child.failure.max.retry.times", maxChildRetryTimes);
		}
		
		protected void beforeRun() {
			startTime = new Date();
			childResult.setStartWhen(TimeUtils.format(startTime, statDateFormat));
		}
		
		@Override
		public void execute() {
			beforeRun();
			try {
				doChildBody();
			} finally {
				afterRun();
			}
		}
		
		protected void afterRun() {
			finishTime = new Date();
			childResult.setFinishWhen(TimeUtils.format(finishTime, statDateFormat));
			childResult.setTimeTaken(finishTime.getTime() - startTime.getTime());
			// record finished child worker
			counter.incrementAndGet();
		}

		public void doChildBody() {
			Exception result = null;
			// execute the task statement
			result = executeChild();
			if(result != null) {
				// retry
				int retryTimes = maxRetryTimes;
				for (int i = retryTimes; i > 0; i--) {
					COG.info("Child retried: id=" + id + ", retryTimes=" + (retryTimes - i + 1) + ", cause=" + result);
					result = executeChild();
					if(result == null) {
						break;
					}
				}
			}
			// set result
			if(result != null) {
				childResult.setStatus(Status.FAILURE);
				childResult.getFailureCauses().add(result);
			} else {
				childResult.setStatus(Status.SUCCESS);
			}
		}
		
		private Exception executeChild() {
			Exception result = null;
			try {
				process(element);
			} catch (Exception e) {
				result = e;
			}
			return result;
		}

		public E getElement() {
			return element;
		}
		
		@Override
		public ChildTaskExecutionResult getResult() {
			return childResult;
		}
		
		@Override
		public boolean isTerminateWhenFailure() {
			return AbstractIterableTaskExecutor.this.isTerminateWhenFailure();
		}

		@Override
		public void setTerminateWhenFailure(boolean terminateWhenFailure) {
			AbstractIterableTaskExecutor.this.setTerminateWhenFailure(terminateWhenFailure);			
		}

	}
	
	public class ChildTaskExecutionResult extends DefaultExecutionResult {
		
		private ChildTaskExecutor childTaskExecutor;
		
		public ChildTaskExecutor getChildTaskExecutor() {
			return childTaskExecutor;
		}
		
		public void setChildTaskExecutor(ChildTaskExecutor childTaskExecutor) {
			this.childTaskExecutor = childTaskExecutor;
		}
	}
	
	public static class ChildTaskExecutionException extends Exception {

		private static final long serialVersionUID = 3844442059837186731L;
		private ExecutionResult childResult;
		
		public ChildTaskExecutionException(ExecutionResult result) {
			childResult = result;
		}

		public ExecutionResult getChildResult() {
			return childResult;
		}
		
	}

}
