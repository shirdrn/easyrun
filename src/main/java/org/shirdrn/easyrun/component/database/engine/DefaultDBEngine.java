package org.shirdrn.easyrun.component.database.engine;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.shirdrn.easyrun.common.ConnectionPoolFactory;
import org.shirdrn.easyrun.common.ConnectionPoolService;
import org.shirdrn.easyrun.component.connectionpool.DefaultConnectionPoolFactory;
import org.shirdrn.easyrun.component.connectionpool.JDBCConnectionPool;
import org.shirdrn.easyrun.component.database.DBCollection;
import org.shirdrn.easyrun.component.database.DBResult;
import org.shirdrn.easyrun.component.database.DefaultDBCollection;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class DefaultDBEngine implements DBEngine {

	private String connectionPoolClassName = JDBCConnectionPool.class.getName();
	private final ConnectionPoolFactory connectionPoolFactory;
	private final ConnectionPoolService pool;
	
	public DefaultDBEngine() {
		this(FactoryUtils.getFactory(DefaultConnectionPoolFactory.class.getName() , ConnectionPoolFactory.class));
	}
	
	public DefaultDBEngine(ConnectionPoolFactory connectionPoolFactory) {
		super();
		this.connectionPoolFactory = connectionPoolFactory;
		this.pool = connectionPoolFactory.get(connectionPoolClassName);
	}
	
	@Override
	public void close() throws IOException {
		connectionPoolFactory.close(pool);
	}

	@Override
	public DBCollection<DBResult> executeQuery(SQLBuilder builder) throws SQLException {
		ResultSet rs = builder.getPreparedStatement().executeQuery();
		DBCollection<DBResult> c = new DefaultDBCollection(rs);
		return c;
	}

	@Override
	public int executeUpdate(SQLBuilder builder) throws SQLException {
		return builder.getPreparedStatement().executeUpdate();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return pool.getConnection().prepareStatement(sql);
	}


}
