package org.shirdrn.easyrun.common;

/**
 * A {@link TaskExecutor} could be break or not when a exception
 * or error occurs. If permitted then the program is running forwards.
 * 
 * @author Shirdrn
 */
public interface Breakable {

	boolean isTerminateWhenFailure();
	void setTerminateWhenFailure(boolean terminateWhenFailure);
}
