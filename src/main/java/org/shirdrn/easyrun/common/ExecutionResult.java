package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.common.TaskExecutor.Status;

public interface ExecutionResult {

	Status getStatus();
	void setStatus(Status status);
	
	String getStartWhen();
	void setStartWhen(String startWhen);
	
	String getFinishWhen();
	void setFinishWhen(String finishWhen);
	
	long getTimeTaken();
	void setTimeTaken(long timeTaken);
	
	Exception getFailureCause();
	void setFailureCause(Exception failureCause);
	
}
