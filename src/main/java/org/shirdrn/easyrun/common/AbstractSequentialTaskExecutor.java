package org.shirdrn.easyrun.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSequentialTaskExecutor<E> extends AbstractIterableTaskExecutor<E> {

	private static final Log LOG = LogFactory.getLog(AbstractSequentialTaskExecutor.class);
	private TaskExecutor<ChildTaskExecutionResult> errorChildExecutor;
	
	public AbstractSequentialTaskExecutor() {
		super();
	}
	
	@Override
	protected void startChildTaskExecutor(E element) throws ChildTaskExecutionException {
		TaskExecutor<ChildTaskExecutionResult> call = new ChildTaskExecutor(element);
		call.execute();
		ExecutionResult childResult = call.getResult();
		if(childResult.getStatus() == Status.FAILURE) {
			errorChildExecutor = call;
			LOG.info("Child error to execute: childResult=" + childResult);
			throw new ChildTaskExecutionException(childResult);
		}		
	}

	@Override
	protected TaskExecutor<ChildTaskExecutionResult> getErrorChildTaskExecutor() {
		return errorChildExecutor;
	}

}
