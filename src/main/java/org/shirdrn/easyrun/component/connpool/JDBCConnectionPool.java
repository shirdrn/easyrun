package org.shirdrn.easyrun.component.connpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.ContextReadable;
import org.shirdrn.easyrun.common.ConnectionPool;
import org.shirdrn.easyrun.utils.PropertiesConfiguration;

public final class JDBCConnectionPool implements ConnectionPool {
	
	private static final Log LOG = LogFactory.getLog(JDBCConnectionPool.class);
	private static String JDBC_PROPERTIES = "jdbc.properties";
	private final ContextReadable configurationReader;
	private String jdbcUrl;
	private String config;
	
	public JDBCConnectionPool() {
		this.config = JDBC_PROPERTIES;
		this.configurationReader = new PropertiesConfiguration(config);
		try {
			String driverClass = configurationReader.get("jdbc.driverClass");
			LOG.info("JDBC Driver: driverClass=" + driverClass);
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		jdbcUrl = configurationReader.get("jdbc.jdbcUrl");
		LOG.info("JDBC url; url=" + jdbcUrl);
	}
	
	@Override
	public synchronized final Connection getConnection() {
		try {
			return DriverManager.getConnection(jdbcUrl);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
	}

	@Override
	public void release(Connection connection) {
		try {
			if(connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
