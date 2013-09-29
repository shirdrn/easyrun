package org.shirdrn.easyrun.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractSimpleTaskLExecutor extends AbstractTaskExecutor<DefaultExecutionResult> implements SingleSQLMaker {

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
	
	@Override
	public void doBody() {
		startWhen = new Date();
		Connection conn = null;
		try {
			conn = connectionPool.getConnection();
			String[] sqls = makeSQLs();
			Statement stmt = conn.createStatement();
//			executeBatchDDL(stmt, sqls);
			status = Status.SUCCESS;
		} catch (SQLException e) {
			executionResult.setCause(e);
			status = Status.FAILURE;
		} finally {
			connectionPool.release(conn);
		}
	}
	
}
