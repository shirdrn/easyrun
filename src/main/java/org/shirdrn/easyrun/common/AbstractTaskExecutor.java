package org.shirdrn.easyrun.common;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.config.Configuration;

public abstract class AbstractTaskExecutor<T> implements TaskExecutor<T> {

	private static final Log LOG = LogFactory.getLog(AbstractTaskExecutor.class);
	protected String name;
	protected String statDateFormat = "yyyyMMddHHmmss";
	protected int maxRetryTimes = 0;
	protected T executionResult;
	protected Date startWhen;
	protected Date finishWhen;
	protected long timeTaken;

	private boolean terminateWhenFailure = true;
	
	public AbstractTaskExecutor() {
		super();
		name = getName();
	}
	
	@Override
	public void configure(Configuration config) {
		maxRetryTimes = config.getRContext().getInt("common.failure.max.retry.times", maxRetryTimes);
		statDateFormat = config.getRContext().get("common.stat.date.format", "yyyyMMddHHmmss");
	}
	
	@Override
	public void execute() {
		startWhen = new Date();
		try {
			doBody();
		} catch (Exception e) {
			Exception retryResult = retry(e);
			if(retryResult != null) {
				throw new RuntimeException("Failed after retry to execute: ", retryResult);
			}
		} finally {
			finishWhen = new Date();
			timeTaken = finishWhen.getTime() - startWhen.getTime();
		}
		
	}
	
	protected Exception retry(Exception e) {
		Exception result = e;
		for(int i=maxRetryTimes; i>0; i--) {
			LOG.info("Parent retried: name=" + name +", retryTimes=" + (maxRetryTimes - i + 1) + ", cause=" + result);
			try {
				doBody();
			} catch (Exception ex) {
				result = ex;
			}
			if(result == null) {
				break;
			}
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
	
	@Override
	public boolean isTerminateWhenFailure() {
		return terminateWhenFailure;
	}

	@Override
	public void setTerminateWhenFailure(boolean terminateWhenFailure) {
		this.terminateWhenFailure = terminateWhenFailure;		
	}
	
}
