package org.shirdrn.easyrun.component.connectionpool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.shirdrn.easyrun.common.config.ContextReadable;
import org.shirdrn.easyrun.common.config.PropertiesConfiguration;
import org.shirdrn.easyrun.component.common.AbstractConnectionPool;

/**
 * DBCP connection pool component.
 * 
 * @author Shirdrn
 */
public class DBCPConnectionPool extends AbstractConnectionPool {

	private final ContextReadable context;
	
	public DBCPConnectionPool() {
		this("dbcp.properties");
	}
	
	public DBCPConnectionPool(String config) {
		super(config);
		context = new PropertiesConfiguration(config);
		setDs();
	}
	
	private void setDs() {
		dataSource = new BasicDataSource();
		BasicDataSource ds = (BasicDataSource) dataSource;
		ds.setUrl(context.get("jdbc.url", null));
		ds.setDriverClassName(context.get("jdbc.driverClassName", null));
		ds.setUsername(context.get("jdbc.username", ""));
		ds.setPassword(context.get("jdbc.password", ""));
		ds.setInitialSize(context.getInt("initialSize", 1));
		ds.setMaxActive(context.getInt("maxActive", 1));
		ds.setMinIdle(context.getInt("minIdle", 0));
		ds.setMaxIdle(context.getInt("maxIdle", 0));
		ds.setMaxWait(context.getLong("maxWait", 0L));
	}
	
	@Override
	public void close() throws IOException {
		BasicDataSource ds = (BasicDataSource) dataSource;
		if(ds != null) {
			try {
				ds.close();
			} catch (SQLException e) {
				e.printStackTrace();
			};
		}
	}

	@Override
	public synchronized final Connection getConnection() {
		BasicDataSource ds = (BasicDataSource) dataSource;
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
