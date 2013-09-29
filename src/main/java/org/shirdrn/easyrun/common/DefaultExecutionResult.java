package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.common.TaskExecutor.Status;

public class DefaultExecutionResult implements ExecutionResult {

	private Status status;
	private String startWhen;
	private String finishWhen;
	private long timeTaken;
	private Exception cause;
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getStartWhen() {
		return startWhen;
	}
	public void setStartWhen(String startWhen) {
		this.startWhen = startWhen;
	}
	public String getFinishWhen() {
		return finishWhen;
	}
	public void setFinishWhen(String finishWhen) {
		this.finishWhen = finishWhen;
	}
	public long getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}
	public Exception getCause() {
		return cause;
	}
	public void setCause(Exception cause) {
		this.cause = cause;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("status=" + status + ", ")
		.append("startWhen=" + startWhen + ", ")
		.append("finishWhen=" + finishWhen + ", ")
		.append("timeTaken=" + timeTaken + ", ");
		if(cause != null) {
			sb.append("cause=" + cause.getMessage());
		}
		return sb.toString();
	}
	
}
