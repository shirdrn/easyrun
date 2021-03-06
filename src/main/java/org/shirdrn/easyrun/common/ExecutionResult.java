package org.shirdrn.easyrun.common;

import java.util.List;

import org.shirdrn.easyrun.common.TaskExecutor.Status;

/**
 * The protocol of the execution result of a 
 * {@link TaskExecutor} instance.
 * 
 * @author Shirdrn
 */
public interface ExecutionResult {

	Status getStatus();
	void setStatus(Status status);
	
	String getStartWhen();
	void setStartWhen(String startWhen);
	
	String getFinishWhen();
	void setFinishWhen(String finishWhen);
	
	long getTimeTaken();
	void setTimeTaken(long timeTaken);
	
	List<Exception> getFailureCauses();
	
}
