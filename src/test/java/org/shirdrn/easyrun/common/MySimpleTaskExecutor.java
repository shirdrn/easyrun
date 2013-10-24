package org.shirdrn.easyrun.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MySimpleTaskExecutor extends AbstractDefaultTaskExecutor {

	private static final Log LOG = LogFactory.getLog(MySimpleTaskExecutor.class);
	
	@Override
	protected void doBody() throws Exception {
		for(int i=0; i<10; i++) {
			LOG.info("Print " + i);
		}
	}
	
}