package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractSimpleTaskLExecutor extends AbstractTaskExecutor<ExecutionResult> {

	public AbstractSimpleTaskLExecutor() {
		super();
		executionResult = new DefaultExecutionResult();
	}
	
	@Override
	public void execute() {
		super.execute();
		// set execution result
		executionResult.setStartWhen(TimeUtils.format(startWhen, statDateFormat));
		executionResult.setFinishWhen(TimeUtils.format(finishWhen, statDateFormat));
		executionResult.setTimeTaken(timeTaken);
		executionResult.setStatus(status);
	}

}
