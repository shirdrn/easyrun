package org.shirdrn.easyrun.component.connpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.common.ConnectionPoolFactory;
import org.shirdrn.easyrun.common.ConnectionPoolService;
import org.shirdrn.easyrun.common.ObjectFactory;
import org.shirdrn.easyrun.component.connectionpool.DefaultConnectionPoolFactory;
import org.shirdrn.easyrun.component.connectionpool.JDBCConnectionPool;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestConnectionPoolFactory {

	private static final Log LOG = LogFactory.getLog(TestConnectionPoolFactory.class);
	String factoryClassName;
	ObjectFactory<String, ConnectionPoolService> connectionPoolFactory;
	ConnectionPoolService connectionPoolService;
	
	@Before
	public void initialize() {
		// create connection pool factory instance
		factoryClassName = DefaultConnectionPoolFactory.class.getName();
		connectionPoolFactory = (ObjectFactory<String, ConnectionPoolService>) FactoryUtils.getFactory(factoryClassName, ConnectionPoolFactory.class);
		assertNotNull(connectionPoolFactory);
	}
	
	@Test
	public void test() {
		// get instance
		String connectionPoolClass = JDBCConnectionPool.class.getName();
		connectionPoolService = connectionPoolFactory.get(connectionPoolClass);
		assertNotNull(connectionPoolService);
		
		// count
		assertEquals(1, connectionPoolFactory.count());
		
		// iterate
		Iterator<Entry<String, ConnectionPoolService>> iter = connectionPoolFactory.iterator();
		while(iter.hasNext()) {
			Entry<String, ConnectionPoolService> kv = iter.next();
			LOG.info("key=" + kv.getKey() + ", value=" + kv.getValue());
		}
		
		// close connection pool factory
		connectionPoolFactory.close(connectionPoolService);
	}
	
	@Test
	public void testCloseAll() {
		connectionPoolFactory.closeAll();
	}
	
}
