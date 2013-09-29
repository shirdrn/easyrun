package org.shirdrn.easyrun.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.Configuration;
import org.shirdrn.easyrun.common.TaskExecutor;

public final class TaskExecutorChainBuilder {

	private static final Log LOG = LogFactory.getLog(TaskExecutorChainBuilder.class);
	private final LinkedHashMap<TaskExecutor<?>, TaskExecutorBuilder<? extends TaskExecutor<?>>> builders = 
			new LinkedHashMap<TaskExecutor<?>, TaskExecutorBuilder<? extends TaskExecutor<?>>>();
	private final LinkedHashMap<Class<? extends TaskExecutor<?>>, TaskExecutor<?>> classes = 
			new LinkedHashMap<Class<? extends TaskExecutor<?>>, TaskExecutor<?>>();
	private final LinkedList<TaskExecutor<?>> execChain = new LinkedList<TaskExecutor<?>>();
	private final LinkedHashMap<TaskExecutor<?>, Object> resultMap = new LinkedHashMap<TaskExecutor<?>, Object>();
	
	private TaskExecutorBuilder<? extends TaskExecutor<?>> currentBuidler;
	
	public TaskExecutorBuilder<? extends TaskExecutor<?>> chain(Class<? extends TaskExecutor<?>> executorClass) {
		return chain(executorClass, new Object[] {});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TaskExecutorBuilder<? extends TaskExecutor<?>> chain(Class<? extends TaskExecutor<?>> executorClass, Object... parameters) {
		TaskExecutor<?> instance = null;
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
	
	public void fireChain(Configuration config) {
		for(TaskExecutor<?> executor : execChain) {
			executor.configure(config);
			executor.execute();
			resultMap.put(executor, executor.getResult());
			TaskExecutorBuilder<? extends TaskExecutor<?>> builder = builders.get(executor);
			if(builder.isTerminateWhenFailure()) {
				LOG.info("");
				break;
			}
		}
	}
	
	public TaskExecutor<?> getFirst() {
		return execChain.getFirst();
	}
	
	public TaskExecutor<?> getLast() {
		return execChain.getLast();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getResult(Class<? extends TaskExecutor<T>> executorClass, Class<T> resultType) {
		T result = null;
		TaskExecutor<?> instance = classes.get(executorClass);
		if(instance != null) {
			result = (T) resultMap.get(instance);
		}
		return result;
	}
	
	public class TaskExecutorBuilder<T extends TaskExecutor<?>>  {
		
		private TaskExecutorChainBuilder chainBuilder;
		private T taskExecutor;
		private boolean terminateWhenFailure = false;
		
		public TaskExecutorBuilder(TaskExecutorChainBuilder chainBuilder, T taskExecutor) {
			this.taskExecutor = taskExecutor;
			this.chainBuilder = chainBuilder;
		}
		
		public TaskExecutorChainBuilder chainBuilder() {
			return chainBuilder;
		}

		protected T getTaskExecutor() {
			return taskExecutor;
		}

		protected boolean isTerminateWhenFailure() {
			return terminateWhenFailure;
		}

		protected void setTerminateWhenFailure(boolean terminateWhenFailure) {
			this.terminateWhenFailure = terminateWhenFailure;
		}
		
		public TaskExecutorChainBuilder terminateWhenFailure() {
			return terminateWhenFailure(true);
		}
		
		public TaskExecutorChainBuilder terminateWhenFailure(boolean terminate) {
			currentBuidler.setTerminateWhenFailure(terminate);
			return chainBuilder;
		}
		
	}
	
}
