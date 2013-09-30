package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.config.Configuration;

public interface TaskExecutor<T> {
	
	void execute();
	T getResult();
	void configure(Configuration config);
	
	enum Status {
		UNKNOWN,
		SUCCESS,
		FAILURE
	}
}
