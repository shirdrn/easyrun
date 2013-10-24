package org.shirdrn.easyrun.common.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.ExecutionResult;
import org.shirdrn.easyrun.common.TaskExecutor;
import org.shirdrn.easyrun.common.TaskExecutor.Status;
import org.shirdrn.easyrun.utils.ReflectionUtils;

public class DefaultTaskExecutorChainBuilder implements TaskExecutorChainBuilder {

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
	
	@Override
	public TaskExecutorChainBuilder chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass) {
		return chain(executorClass, (Object[]) null);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TaskExecutorChainBuilder chain(Class<? extends TaskExecutor<ExecutionResult>> executorClass, Object... parameters) {
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
		return this;
	}
	
	@Override
	public void fireChain() {
		for(TaskExecutor<ExecutionResult> executor : execChain) {
			LOG.info("Execute task: executor=" + executor.getClass().getName());
			executor.configure(configuration);
			executor.execute();
			LOG.info("Done: result=" + executor.getResult());
			resultMap.put(executor, executor.getResult());
			TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> builder = builders.get(executor);
			if((executor.getResult() == null 
					|| executor.getResult().getStatus() != Status.SUCCESS)) {
				if(executor.isTerminateWhenFailure()) {
					LOG.info("Terminate task: " + builder.getTaskExecutor().getClass().getName());
					break;
				}
			}
		}
	}
	
	@Override
	public TaskExecutor<ExecutionResult> getFirst() {
		return execChain.getFirst();
	}
	
	@Override
	public TaskExecutor<ExecutionResult> getLast() {
		return execChain.getLast();
	}
	
	@Override
	public ExecutionResult getResult(Class<? extends TaskExecutor<ExecutionResult>> executorClass) {
		ExecutionResult result = null;
		TaskExecutor<ExecutionResult> instance = classes.get(executorClass);
		if(instance != null) {
			result = resultMap.get(instance);
		}
		return result;
	}
	
	@Override
	public TaskExecutorChainBuilder terminateWhenFailure() {
		return currentBuidler.terminateWhenFailure();
	}

	@Override
	public TaskExecutorChainBuilder terminateWhenFailure(boolean terminate) {
		return currentBuidler.terminateWhenFailure(terminate);
	}

	@Override
	public TaskExecutorBuilder<? extends TaskExecutor<ExecutionResult>> getCurrentTaskExecutorBuilder() {
		return currentBuidler;
	}
	
	@Override
	public boolean isTerminateWhenFailure() {
		return currentBuidler.isTerminateWhenFailure();
	}
	
	@Override
	public void setTerminateWhenFailure(boolean terminateWhenFailure) {
		currentBuidler.setTerminateWhenFailure(terminateWhenFailure);		
	}
	
	
	
	public class TaskExecutorBuilder<T extends TaskExecutor<ExecutionResult>> implements TaskExecutorConfigurable  {
		
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

		public DefaultTaskExecutorChainBuilder terminateWhenFailure() {
			return terminateWhenFailure(true);
		}
		
		public DefaultTaskExecutorChainBuilder terminateWhenFailure(boolean terminate) {
			currentBuidler.getTaskExecutor().setTerminateWhenFailure(terminate);
			return chainBuilder;
		}
		
		@Override
		public boolean isTerminateWhenFailure() {
			return terminateWhenFailure;
		}

		@Override
		public void setTerminateWhenFailure(boolean terminateWhenFailure) {
			this.terminateWhenFailure = terminateWhenFailure;
		}
		
	}

}
