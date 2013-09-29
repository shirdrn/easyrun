package org.shirdrn.easyrun.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSequentialTaskExecutor extends AbstractIterableTaskExecutor {

	private static final Log LOG = LogFactory.getLog(AbstractSequentialTaskExecutor.class);
	private TaskExecutor<ChildTaskExecutionResult> badChildExecutor;
	
	public AbstractSequentialTaskExecutor() {
		super();
	}
	
	@Override
	public void doBody() {
		super.doBody();		
	}

	@Override
	protected void fireChildSQLs(String[] childSqls) throws ChildTaskExecutionException {
		TaskExecutor<ChildTaskExecutionResult> call = new ChildTaskExecutor(childSqls);
		call.execute();
		ExecutionResult childResult = call.getResult();
		if(childResult.getStatus() == Status.FAILURE) {
			badChildExecutor = call;
			LOG.info("Child error to execute: childResult=" + childResult);
			throw new ChildTaskExecutionException(childResult);
		}
	}
	
	@Override
	protected TaskExecutor<ChildTaskExecutionResult> getErrorChildTaskExecutor() {
		return badChildExecutor;
	}

}
