package org.shirdrn.easyrun.component.database.engine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.component.database.DBCollection;
import org.shirdrn.easyrun.component.database.DBResult;

public class TestDefaultDBEngine {

	private static final Log LOG = LogFactory.getLog(TestDefaultDBEngine.class);
	DBEngine engine;
	
	@Before
	public void initialize() {
		engine = new DefaultDBEngine();
	}
	
	@Test
	public void executeQuery() throws SQLException, IOException {
		String sql = "SELECT post_author,post_title FROM wp_posts WhERE id > ? AND post_author = ?";
		SQLBuilder builder = new DefaultSQLBuilder(engine);
		builder.build(sql)
			.set(1, 0)
			.set(2, 3);
		DBCollection<DBResult> c = iterate(builder);
		c.close();
	}
	
	@Test
	public void executeQueryWithIndexes() throws SQLException, IOException {
		String sql = "SELECT post_author,post_title FROM wp_posts WhERE id > ? AND post_author = ?";
		SQLBuilder builder = new DefaultSQLBuilder(engine);
		builder.build(sql)
			.set(0)
			.set(3);
		DBCollection<DBResult> c = iterate(builder);
		c.close();
	}
	
	@Test
	public void reset() throws SQLException, IOException {
		String sql = "SELECT post_author,post_title FROM wp_posts WhERE id > ? AND post_author = ?";
		SQLBuilder builder = new DefaultSQLBuilder(engine);
		builder.build(sql)
			.set(0)
			.set(3);
		LOG.info("Before reset: ");
		LOG.info("builder=" + builder);
		DBCollection<DBResult> c = iterate(builder);
		c.close();
		
		builder.reset();
		sql = "SELECT post_author,post_title FROM wp_posts WhERE id > ? AND post_author = ?";
		builder.build(sql)
		.set(1, -1)
		.set(2, 2);
		LOG.info("After reset: ");
		LOG.info("builder=" + builder);
		c = iterate(builder);
		c.close();
	}

	private DBCollection<DBResult> iterate(SQLBuilder builder) throws SQLException {
		DBCollection<DBResult> c = engine.executeQuery(builder);
		Iterator<DBResult> iter = c.iterator();
		while(iter.hasNext()) {
			DBResult result = iter.next();
			LOG.info(result.getLong("post_author") + " => " + result.getString("post_title"));
		}
		return c;
	}
	
	@After
	public void destroy() throws IOException {
		engine.close();
	}
}
