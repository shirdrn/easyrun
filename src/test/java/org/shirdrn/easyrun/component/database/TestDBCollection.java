package org.shirdrn.easyrun.component.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.common.ConnectionPoolFactory;
import org.shirdrn.easyrun.common.ConnectionPoolService;
import org.shirdrn.easyrun.component.connectionpool.DefaultConnectionPoolFactory;
import org.shirdrn.easyrun.component.connectionpool.JDBCConnectionPool;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestDBCollection {

	private static final Log LOG = LogFactory.getLog(TestDBCollection.class);
	DBCollection<DBResult> collection;
	ConnectionPoolService pool;
	
	@Before
	public void initialize() {
		String factoryClassName = DefaultConnectionPoolFactory.class.getName();
		ConnectionPoolFactory factory = FactoryUtils.getFactory(factoryClassName, ConnectionPoolFactory.class);
		
		String connectionPoolClassName = JDBCConnectionPool.class.getName();
		pool = factory.get(connectionPoolClassName);
	}
	
	@Test
	public void testIteration() throws Exception {
		
		Connection conn = pool.getConnection();
		PreparedStatement stmt = conn.prepareStatement("SELECT post_title FROM wp_posts WhERE id > ?");
		stmt.setLong(1, 0);
		ResultSet rs = stmt.executeQuery();
		collection = new DefaultDBCollection(rs);
		Iterator<DBResult> iter = collection.iterator();
		
		LOG.info("### Round 01 ###");
		while(iter.hasNext()) {
			LOG.info(iter.next().getString(1));
		}
		
		collection.reset();
		LOG.info("### Round 02 ###");
		while(iter.hasNext()) {
			LOG.info(iter.next().getString("post_title"));
		}
		
		pool.release(conn);
		collection.close();
	}
	
	@After
	public void destroy() {
		FactoryUtils.closeAll();
	}
	
}
