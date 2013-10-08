package org.shirdrn.easyrun.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.ExecutionResult;
import org.shirdrn.easyrun.common.TaskExecutor;
import org.shirdrn.easyrun.common.TaskExecutor.Status;
import org.shirdrn.easyrun.config.Configuration;

public class DefaultTaskExecutorChainBuilder {

	private static final Log LOG = LogFactory.getLog(DefaultTaskExecutorChainBuilder.class);
	private final LinkedHashMap<TaskExecutor<ExecutionResult>, TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>>> builders = 
			new LinkedHashMap<TaskExecutor<ExecutionResult>, TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>>>();
	private final LinkedHashMap<Class<? extends TaskExecutor<ExecutionResult>>, TaskExecutor<ExecutionResult>> classes = 
			new LinkedHashMap<Class<? extends TaskExecutor<ExecutionResult>>, TaskExecutor<ExecutionResult>>();
	private final LinkedList<TaskExecutor<ExecutionResult>> execChain = new LinkedList<TaskExecutor<ExecutionResult>>();
	private final LinkedHashMap<TaskExecutor<ExecutionResult>, ExecutionResult> resultMap = new LinkedHashMap<TaskExecutor<ExecutionResult>, ExecutionResult>();
	
	private TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> currentBuidler;
	private Configuration configuration;	
	
	public DefaultTaskExecutorChainBuilder(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass) {
		return chain(executorClass, new Object[] {});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass, Object... parameters) {
		TaskExecutor<ExecutionResult> instance = null;
		if(parameters != null) {
			instance = ReflectionUtils.getInstance(executorClass, parameters);
		} else {
			instance = ReflectionUtils.getInstance(executorClass);
		}
		currentBuidler = new TaskExecutorBuilder(this, instance);
		builders.put(instance, currentBuidler);
		execChain.addLast(instance);
		classes.put(executorClass, instance);
		return currentBuidler;
	}
	
	public void fireChain() {
		for(TaskExecutor<ExecutionResult> executor : execChain) {
			LOG.info("Execute task: executor=" + executor.getClass().getName());
			executor.configure(configuration);
			executor.execute();
			LOG.info("Done: result=" + executor.getResult());
			resultMap.put(executor, executor.getResult());
			TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> builder = builders.get(executor);
			if((executor.getResult() == null || executor.getResult().getStatus() != Status.SUCCESS) 
					&& builder.isTerminateWhenFailure()) {
				LOG.info("Terminate task: " + builder.getTaskExecutor().getClass().getName());
				break;
			}
		}
	}
	
	public TaskExecutor<ExecutionResult> getFirst() {
		return execChain.getFirst();
	}
	
	public TaskExecutor<ExecutionResult> getLast() {
		return execChain.getLast();
	}
	
	public ExecutionResult getResult(Class<? extends TaskExecutor<ExecutionResult>> executorClass) {
		ExecutionResult result = null;
		TaskExecutor<ExecutionResult> instance = classes.get(executorClass);
		if(instance != null) {
			result = resultMap.get(instance);
		}
		return result;
	}
	
	public class TaskExecutorBuilder<T extends TaskExecutor<ExecutionResult>>  {
		
		private DefaultTaskExecutorChainBuilder chainBuilder;
		private T TaskExecutor;
		private boolean terminateWhenFailure = true;
		
		public TaskExecutorBuilder(DefaultTaskExecutorChainBuilder chainBuilder, T TaskExecutor) {
			this.TaskExecutor = TaskExecutor;
			this.chainBuilder = chainBuilder;
		}
		
		public DefaultTaskExecutorChainBuilder chainBuilder() {
			return chainBuilder;
		}

		protected T getTaskExecutor() {
			return TaskExecutor;
		}

		protected boolean isTerminateWhenFailure() {
			return terminateWhenFailure;
		}

		protected void setTerminateWhenFailure(boolean terminateWhenFailure) {
			this.terminateWhenFailure = terminateWhenFailure;
		}
		
		public DefaultTaskExecutorChainBuilder terminateWhenFailure() {
			return terminateWhenFailure(true);
		}
		
		public DefaultTaskExecutorChainBuilder terminateWhenFailure(boolean terminate) {
			currentBuidler.setTerminateWhenFailure(terminate);
			return chainBuilder;
		}
		
	}
	
}
