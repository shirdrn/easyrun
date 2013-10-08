package org.shirdrn.easyrun.common;

public class MySimpleTaskExecutor extends AbstractSimpleTaskLExecutor {

	@Override
	protected void doBody() throws Exception {
		for(int i=0; i<10; i++) {
			System.out.println("print " + i);
		}
	}
	
}