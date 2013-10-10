package org.shirdrn.easyrun.component.threadpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.common.ObjectFactory;
import org.shirdrn.easyrun.common.ThreadPoolFactory;
import org.shirdrn.easyrun.common.ThreadPoolService;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.config.ContextReadable;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestThreadPoolFactory {

	private static final Log LOG = LogFactory.getLog(TestThreadPoolFactory.class);
	Configuration configuration;
	String factoryClassName;
	ObjectFactory<ContextReadable, ThreadPoolService> threadPoolFactory;
	ThreadPoolService threadPoolService;
	
	@Before
	public void initialize() {
		// create thread pool factory instance
		factoryClassName = DefaultThreadPoolFactory.class.getName();
		configuration = FactoryUtils.getDefaultConfiguration();
		threadPoolFactory = (ObjectFactory<ContextReadable, ThreadPoolService>) FactoryUtils.getFactory(factoryClassName, ThreadPoolFactory.class);
		assertNotNull(threadPoolFactory);
	}
	
	@Test
	public void test() {
		// get instance
		threadPoolService = threadPoolFactory.get(configuration.getRContext());
		assertNotNull(threadPoolService);
		
		// count
		assertEquals(1, threadPoolFactory.count());
		
		// iterate
		Iterator<Entry<ContextReadable, ThreadPoolService>> iter = threadPoolFactory.iterator();
		while(iter.hasNext()) {
			Entry<ContextReadable, ThreadPoolService> kv = iter.next();
			LOG.info("key=" + kv.getKey() + ", value=" + kv.getValue());
		}
		
		// close thread pool factory
		threadPoolFactory.close(threadPoolService);
	}
	
	@Test
	public void testCloseAll() {
		threadPoolFactory.closeAll();
	}
	
}
