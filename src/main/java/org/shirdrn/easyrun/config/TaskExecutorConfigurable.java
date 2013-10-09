package org.shirdrn.easyrun.config;


public interface TaskExecutorConfigurable {

	TaskExecutorChainBuilder terminateWhenFailure();
	TaskExecutorChainBuilder terminateWhenFailure(boolean terminate);
	boolean isTerminateWhenFailure();
	
}
