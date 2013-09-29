package org.shirdrn.easyrun.common;

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
