package org.shirdrn.easyrun.common;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.AbstractIterableTaskExecutor.ChildTaskExecutionException;
import org.shirdrn.easyrun.utils.TimeUtils;

/**
 * Using {@link DefaultExecutionResult} as the returned execution result object.
 * 
 * @author Shirdrn
 */
public abstract class AbstractDefaultTaskExecutor extends AbstractTaskExecutor<ExecutionResult> {
	
	private static final Log LOG = LogFactory.getLog(AbstractDefaultTaskExecutor.class);
	
	public AbstractDefaultTaskExecutor() {
		super();
		executionResult = new DefaultExecutionResult();
	}
	
	@Override
	public void execute() {
		try {
			startWhen = new Date();
			executionResult.setStartWhen(TimeUtils.format(startWhen, statDateFormat));
			doBody();
			executionResult.setStatus(Status.SUCCESS);
		} catch(Exception e) {
			Exception result = null;
			if(! (e instanceof ChildTaskExecutionException)) {
				LOG.info("Retry, exception=", e);
				result = retry(e);
				if(result != null) {
					executionResult.setStatus(Status.FAILURE);
				} else {
					executionResult.setStatus(Status.SUCCESS);
				}
			}
		} finally {
			finishWhen = new Date();
			// set execution result
			executionResult.setFinishWhen(TimeUtils.format(finishWhen, statDateFormat));
			timeTaken = finishWhen.getTime() - startWhen.getTime();
			executionResult.setTimeTaken(timeTaken);
		}
	}
	
}
