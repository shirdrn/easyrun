package org.shirdrn.easyrun.common;

import java.util.Date;

import org.shirdrn.easyrun.component.connpool.ConnectionPoolFactory;
import org.shirdrn.easyrun.utils.FactoryUtils;

public abstract class AbstractTaskExecutor<T> implements TaskExecutor<T> {

	protected String name;
	protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	protected Status status = Status.UNKNOWN;
	protected ConnectionPool connectionPool;
	protected int maxRetryTimes;
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
		terminateWhenFailure = config.getRContext().getBoolean(
				"common.terminate.when.failure", true);
		maxRetryTimes = config.getRContext().getInt(
				"common.child.failure.max.retry.times", 3);
		
		String connectionPoolClass = config.getRContext().get(
				"component.connection.pool.class", 
				"org.shirdrn.easyrun.component.connpool.JDBCConnectionPool");
		connectionPool = FactoryUtils.getFactory(ConnectionPoolFactory.class).get(connectionPoolClass);
		
	}
	
	@Override
	public void execute() {
		try {
			startWhen = new Date();
			doBody();
		} finally {
			finishWhen = new Date();
			timeTaken = finishWhen.getTime() - startWhen.getTime();
		}
	}
	
	protected abstract void doBody();
	
	protected String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public T getResult() {
		return executionResult;
	}

}
