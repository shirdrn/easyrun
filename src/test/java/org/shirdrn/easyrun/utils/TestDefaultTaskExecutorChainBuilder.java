package org.shirdrn.easyrun.utils;

import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.common.MySequentialTaskExecutor;
import org.shirdrn.easyrun.common.MySimpleTaskExecutor;
import org.shirdrn.easyrun.common.PrintTaskExecutor;
import org.shirdrn.easyrun.config.Configuration;

public class TestDefaultTaskExecutorChainBuilder {

	Configuration configuration;
	DefaultTaskExecutorChainBuilder builder;
	
	@Before
	public void intialize() {
		configuration = FactoryUtils.getDefaultConfiguration();
		builder = new DefaultTaskExecutorChainBuilder(configuration);
	}
	
	@Test
	public void fireChain() {
		builder
		.chain(MySequentialTaskExecutor.class).terminateWhenFailure()
		.chain(MySimpleTaskExecutor.class).terminateWhenFailure()
		.chain(PrintTaskExecutor.class).terminateWhenFailure()
		.fireChain();
	}
	
	
}
