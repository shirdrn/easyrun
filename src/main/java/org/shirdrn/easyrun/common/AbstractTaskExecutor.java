package org.shirdrn.easyrun.common;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.AbstractIterableTaskExecutor.ChildTaskExecutionException;
import org.shirdrn.easyrun.config.Configuration;

public abstract class AbstractTaskExecutor<T> implements TaskExecutor<T> {

	private static final Log LOG = LogFactory.getLog(AbstractTaskExecutor.class);
	protected String name;
	protected String statDateFormat = "yyyyMMddHHmmss";
	protected Status status = Status.UNKNOWN;
	protected int maxRetryTimes = 0;
	protected boolean terminateWhenFailure;
	protected T executionResult;
	protected Date startWhen;
	protected Date finishWhen;
	protected long timeTaken;
	
	public AbstractTaskExecutor() {
		super();
	}
	
	@Override
	public void configure(Configuration config) {
		name = getName();
		terminateWhenFailure = config.getRContext().getBoolean("common.terminate.when.failure", true);
		maxRetryTimes = config.getRContext().getInt("common.failure.max.retry.times", maxRetryTimes);
		statDateFormat = config.getRContext().get("common.stat.date.format", "yyyyMMddHHmmss");
	}
	
	@Override
	public void execute() {
		try {
			startWhen = new Date();
			doBody();
			status = Status.SUCCESS;
		} catch(Exception e) {
			Exception result = null;
			if(! (e instanceof ChildTaskExecutionException)) {
				for(int i=maxRetryTimes; i>0; i--) {
					result = executeAgain();
					if(result != null) {
						LOG.info("Parent retried: retryTimes=" + (maxRetryTimes - i + 1) + ", status=" + status);
					} else {
						LOG.info("Parent retried: retryTimes=" + (maxRetryTimes - i + 1) + ", status=" + status);
						break;
					}
				}
			}
			if(result != null) {
				status = Status.FAILURE;
			} else {
				status = Status.SUCCESS;
			}
		} finally {
			finishWhen = new Date();
			timeTaken = finishWhen.getTime() - startWhen.getTime();
		}
	}
	
	private Exception executeAgain() {
		Exception result = null;
		try {
			doBody();
		} catch (Exception e) {
			result = e;
		}
		return result;
	}
	
	protected abstract void doBody() throws Exception;
	
	protected String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public T getResult() {
		return executionResult;
	}
	
}
