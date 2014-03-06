package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.common.config.Configuration;

/**
 * Task execution protocol. A {@link TaskExecutor} includes the basic
 * logic a task, and can be configured based on {{@link #configure(Configuration)}.
 * When a task finishes executing, we can retrieve the result of task
 * running by invoking {{@link #getResult()}.
 * 
 * @author Shirdrn
 *
 * @param <T> execution result object.
 */
public interface TaskExecutor<T> extends Breakable {
	
	void execute();
	T getResult();
	void configure(Configuration config);
	
	enum Status {
		UNKNOWN,
		SUCCESS,
		FAILURE
	}
}
