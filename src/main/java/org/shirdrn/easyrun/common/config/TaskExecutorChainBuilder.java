package org.shirdrn.easyrun.common.config;

import org.shirdrn.easyrun.common.ExecutionResult;
import org.shirdrn.easyrun.common.TaskExecutor;
import org.shirdrn.easyrun.common.config.DefaultTaskExecutorChainBuilder.TaskExecutorBuilder;

public interface TaskExecutorChainBuilder extends TaskExecutorConfigurable {

	TaskExecutorChainBuilder chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass);
	TaskExecutorChainBuilder chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass, Object... parameters);
	void fireChain();
	TaskExecutor<ExecutionResult> getFirst();
	TaskExecutor<ExecutionResult> getLast();
	ExecutionResult getResult(Class<? extends TaskExecutor<ExecutionResult>> executorClass);
	TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> getCurrentTaskExecutorBuilder();
	
}
