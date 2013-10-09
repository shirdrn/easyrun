package org.shirdrn.easyrun.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestAbstractSequentialTaskExecutor {

	private static final Log LOG = LogFactory.getLog(TestAbstractSequentialTaskExecutor.class);
	TaskExecutor<? extends ExecutionResult> executor;
	Configuration config;
	
	@Before
	public void initialize() {
		config = FactoryUtils.getDefaultConfiguration();
		executor = new MySequentialTaskExecutor();
	}
	
	@Test
	public void testNormal() {
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		LOG.info(result.toString());
	}
	

}
