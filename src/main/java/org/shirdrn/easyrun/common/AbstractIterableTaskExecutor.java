package org.shirdrn.easyrun.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.utils.TimeUtils;

public abstract class AbstractIterableTaskExecutor extends AbstractTaskExecutor<ExecutionResult> implements LoopedSQLMaker {

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
		executionResult.setStartWhen(TimeUtils.format(startWhen, DATE_FORMAT));
		executionResult.setFinishWhen(TimeUtils.format(finishWhen, DATE_FORMAT));
		executionResult.setTimeTaken(timeTaken);
		executionResult.setStatus(status);
	}
	
	@Override
	public void doBody() {
		String sql = makeParentSQL();
		Statement stmt = null;
		Connection conn = connectionPool.getConnection();
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				String[] childSqls = makeChildSQLs(rs);
				if(childSqls != null && childSqls.length > 0) {
					try {
						if(childCaughtError && terminateWhenFailure) {
							LOG.debug("childCaughtError=" + childCaughtError + ", terminateParentWhenChildFailure=" + terminateWhenFailure);
							TaskExecutor<ChildTaskExecutionResult> child = getErrorChildTaskExecutor();
							LOG.info("Child failed to execute: childResult=" + child.getResult());
							throw new ChildTaskExecutionException(child.getResult());
						}
						fireChildSQLs(childSqls);
						totalCount.incrementAndGet();
					} catch (Exception e) {
						LOG.debug(e);
						throw e;
					} finally {
//						closeAll(null, stmt, null);
					}
				} else {
					LOG.warn("After invoke makeChildSQLs(): childSqls==null OR childSqls==[].");
				}
			}
			status = Status.SUCCESS;
		} catch (Exception e) {
			executionResult.setCause(e);
			status = Status.FAILURE;
		} finally {
//			closeAll(null, stmt, rs);
			connectionPool.release(conn);
		}
	}
	
	protected abstract void fireChildSQLs(String[] childSqls) throws ChildTaskExecutionException;

	private static final Log CELOG = LogFactory.getLog(ChildTaskExecutor.class);
	public class ChildTaskExecutor implements TaskExecutor<ChildTaskExecutionResult> {
		
		protected final int id;
		protected final String[] childSQLs;
		protected final Connection conn;
		protected final ChildTaskExecutionResult childResult;
		private Date startTime;
		private Date finishTime;
		
		public ChildTaskExecutor(String[] sqls) {
			super();
			childSQLs = sqls;
			childResult = new ChildTaskExecutionResult();
			childResult.setChildSQLExecutor(this);
			childResult.setChildSQLs(sqls);
			id = workerIdCounter.incrementAndGet();
			childResult.setId(id);
			conn = connectionPool.getConnection();
		}
		
		@Override
		public void configure(Configuration config) {
			// TODO Auto-generated method stub
			
		}
		
		protected void beforeRun() {
			startTime = new Date();
			childResult.setStartWhen(TimeUtils.format(startTime, DATE_FORMAT));
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
			childResult.setFinishWhen(TimeUtils.format(finishTime, DATE_FORMAT));
			childResult.setTimeTaken(finishTime.getTime() - startTime.getTime());
			childResult.setStatus(status);
			connectionPool.release(conn);
			logStat();
			// record finished child worker
			counter.incrementAndGet();
		}

		public void doChildBody() {
			Exception result = null;
			// execute the SQL statement
			result = execute0();
			if(result != null) {
				status = Status.FAILURE;
				// retry
				int retryTimes = maxRetryTimes;
				for (int i = retryTimes; i > 0; i--) {
					result = execute0();
					if(result != null) {
						status = Status.FAILURE;
						CELOG.debug("Retried to execute: retryTimes=" + (retryTimes - i + 1) + ", status=" + status);
					} else {
						status = Status.SUCCESS;
						CELOG.debug("Retried to execute: retryTimes=" + (retryTimes - i + 1) + ", status=" + status);
						break;
					}
				}
			} else {
				status = Status.SUCCESS;
			}
			// set caught exception
			if(result != null) {
				childResult.setCause(result);
			}
		}
		
		private Exception execute0() {
			Exception result = null;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
//				executeBatchDML(stmt, childResult.getChildSQLs());
			} catch (Exception e) {
				result = e;
			} finally {
				if(stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {
						result = e;
					}
				}
			}
			return result;
		}
		
		public void logStat() {
			StringBuffer log = new StringBuffer();
			log.append("Finished child: ")
			.append("name=" + name + ", ")
			.append("sqlsId=" + childResult.getId() + ", ")
			.append("status=" + childResult.getStatus() + ", ")
			.append("start=" + childResult.getStartWhen() + ", ")
			.append("finish=" + childResult.getFinishWhen() + ", ")
			.append("timeTaken=" + childResult.getTimeTaken() + ", ")
			.append("childSQLs=" + Arrays.asList(childResult.getChildSQLs()));
			CELOG.info(log.toString());
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(getClass().getSimpleName())
			.append("[id=").append(id).append(", ")
			.append("childSQLs=").append(childSQLs).append("]");
			return buf.toString();
		}

		@Override
		public ChildTaskExecutionResult getResult() {
			return childResult;
		}

	}
	
	public class ChildTaskExecutionResult extends DefaultExecutionResult {
		private ChildTaskExecutor childSQLExecutor;
		private String[] childSQLs;
		private int id;
		public String[] getChildSQLs() {
			return childSQLs;
		}
		public void setChildSQLs(String[] childSQLs) {
			this.childSQLs = childSQLs;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public ChildTaskExecutor getChildTaskExecutor() {
			return childSQLExecutor;
		}
		public void setChildSQLExecutor(ChildTaskExecutor childSQLExecutor) {
			this.childSQLExecutor = childSQLExecutor;
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(super.toString());
			sb.append(", childId=" + id + ", ")
			.append("childSQLs=" + Arrays.asList(childSQLs));
			return sb.toString();
		}
	}
	
	public class ChildTaskExecutionException extends Exception {

		private static final long serialVersionUID = 3844442059837186731L;
		private ExecutionResult childResult;
		
		public ChildTaskExecutionException(ExecutionResult result) {
			childResult = result;
		}

		public ExecutionResult getChildResult() {
			return childResult;
		}
		
	}

	public boolean isTerminateWhenChildFailure() {
		return terminateWhenFailure;
	}

}
