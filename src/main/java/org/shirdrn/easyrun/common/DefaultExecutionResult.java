package org.shirdrn.easyrun.common;

import java.util.ArrayList;
import java.util.List;

import org.shirdrn.easyrun.common.TaskExecutor.Status;

public class DefaultExecutionResult implements ExecutionResult {

	private String name;
	private Status status;
	private String startWhen;
	private String finishWhen;
	private long timeTaken;
	private List<Exception> failureCauses;
	
	public DefaultExecutionResult() {
		super();
		failureCauses = new ArrayList<>();
		status = Status.UNKNOWN;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public List<Exception> getFailureCauses() {
		return failureCauses;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(name != null) {
			sb.append("name=" + name + ", ");
		}
		sb.append("status=" + status + ", ")
		.append("startWhen=" + startWhen + ", ")
		.append("finishWhen=" + finishWhen + ", ")
		.append("timeTaken=" + timeTaken);
		if(!failureCauses.isEmpty()) {
			sb.append(", failureCauses=" + failureCauses);
		}
		return sb.toString();
	}
	
}
