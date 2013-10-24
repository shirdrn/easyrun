package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.common.config.Configuration;

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
