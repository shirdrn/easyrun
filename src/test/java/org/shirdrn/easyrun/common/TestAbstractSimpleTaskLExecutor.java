package org.shirdrn.easyrun.common;

import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestAbstractSimpleTaskLExecutor {

	TaskExecutor<? extends ExecutionResult> executor;
	Configuration config;
	
	class MySimpleTaskExecutor extends AbstractSimpleTaskLExecutor {

		@Override
		protected void doBody() throws Exception {
			for(int i=0; i<10; i++) {
				System.out.println("print " + i);
			}
		}
		
	}
	
	@Before
	public void initialize() {
		config = FactoryUtils.getDefaultConfiguration();
		executor = new MySimpleTaskExecutor();
	}
	
	@Test
	public void test() {
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		System.out.println(result.toString());
	}
	
}
