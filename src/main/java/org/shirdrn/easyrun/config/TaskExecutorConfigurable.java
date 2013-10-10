package org.shirdrn.easyrun.config;

import org.shirdrn.easyrun.common.Breakable;


public interface TaskExecutorConfigurable extends Breakable {

	TaskExecutorChainBuilder terminateWhenFailure();
	TaskExecutorChainBuilder terminateWhenFailure(boolean terminate);
	
}
