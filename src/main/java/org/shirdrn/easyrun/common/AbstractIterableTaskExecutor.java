package org.shirdrn.easyrun.common;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractIterableTaskExecutor<E> extends AbstractTaskExecutor<ExecutionResult> implements Iterable<E> {

	private static final Log LOG = LogFactory.getLog(AbstractIterableTaskExecutor.class);
	protected final AtomicInteger totalCount = new AtomicInteger(0);
	protected final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicInteger workerIdCounter = new AtomicInteger(0);
	protected volatile boolean childCaughtError = false;
	
	public AbstractIterableTaskExecutor() {
		super();
		executionResult = new DefaultExecutionResult();
	}
	
	@Override
	public void configure(Configuration config) {
		super.configure(config);
	}
	
	protected abstract TaskExecutor<ChildTaskExecutionResult> getErrorChildTaskExecutor();
	
	@Override
	public void execute() {
		super.execute();
		// set execution result
		executionResult.setStartWhen(TimeUtils.format(startWhen, statDateFormat));
		executionResult.setFinishWhen(TimeUtils.format(finishWhen, statDateFormat));
		executionResult.setTimeTaken(timeTaken);
		executionResult.setStatus(status);
	}
	
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
			status = Status.SUCCESS;
		} catch (Exception e) {
			status = Status.FAILURE;
			executionResult.setFailureCause(e);
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
	
	public class ChildTaskExecutor implements TaskExecutor<ChildTaskExecutionResult> {
		
		protected final int id;
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
			childResult.setId(id);
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
			childResult.setStatus(status);
			logStat();
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
					result = executeChild();
					if(result != null) {
						LOG.debug("Child retried: retryTimes=" + (retryTimes - i + 1) + ", status=" + status);
					} else {
						LOG.debug("Child retried: retryTimes=" + (retryTimes - i + 1) + ", status=" + status);
						break;
					}
				}
			}
			// set caught exception
			if(result != null) {
				status = Status.FAILURE;
				childResult.setFailureCause(result);
			} else {
				status = Status.SUCCESS;
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

		public void logStat() {
			StringBuffer log = new StringBuffer();
			log.append("Finished child: ")
			.append("name=" + name + ", ")
			.append("id=" + childResult.getId() + ", ")
			.append("status=" + childResult.getStatus() + ", ")
			.append("start=" + childResult.getStartWhen() + ", ")
			.append("finish=" + childResult.getFinishWhen() + ", ")
			.append("timeTaken=" + childResult.getTimeTaken());
			LOG.info(log.toString());
		}

		public E getElement() {
			return element;
		}
		
		@Override
		public ChildTaskExecutionResult getResult() {
			return childResult;
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(getClass().getSimpleName())
			.append("[id=").append(id);
			return buf.toString();
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
		private int id;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public ChildTaskExecutor getChildTaskExecutor() {
			return childTaskExecutor;
		}
		public void setChildTaskExecutor(ChildTaskExecutor childTaskExecutor) {
			this.childTaskExecutor = childTaskExecutor;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("id=" + id + ", ").append(super.toString());
			return sb.toString();
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
