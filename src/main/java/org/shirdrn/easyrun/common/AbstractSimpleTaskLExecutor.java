package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractSimpleTaskLExecutor extends AbstractTaskExecutor<DefaultExecutionResult> {

	public AbstractSimpleTaskLExecutor() {
		super();
		executionResult = new DefaultExecutionResult();
	}
	
	@Override
	public void execute() {
		super.execute();
		// set execution result
		executionResult.setStartWhen(TimeUtils.format(startWhen, DATE_FORMAT));
		executionResult.setFinishWhen(TimeUtils.format(finishWhen, DATE_FORMAT));
		executionResult.setTimeTaken(timeTaken);
		executionResult.setStatus(status);
	}

}
