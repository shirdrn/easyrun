package org.shirdrn.easyrun.component.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.ConnectionPoolService;
import org.shirdrn.easyrun.common.config.ContextReadable;
import org.shirdrn.easyrun.common.config.PropertiesConfiguration;

public final class JDBCConnectionPool implements ConnectionPoolService {
	
	private static final Log LOG = LogFactory.getLog(JDBCConnectionPool.class);
	private static String JDBC_PROPERTIES = "jdbc.properties";
	private final ContextReadable configurationReader;
	private String jdbcUrl;
	private String user;
	private String password;
	private String config;
	
	public JDBCConnectionPool() {
		this.config = JDBC_PROPERTIES;
		this.configurationReader = new PropertiesConfiguration(config);
		String driverClass = configurationReader.get("jdbc.driverClass");
		try {
			Class.forName(driverClass);
			jdbcUrl = configurationReader.get("jdbc.jdbcUrl");
			user = configurationReader.get("jdbc.user");
			password = configurationReader.get("jdbc.password");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			LOG.info("JDBC: driver=" + driverClass + ", url=" + jdbcUrl + ", user=" + user + ", password=******");
		}
	}
	
	@Override
	public synchronized final Connection getConnection() {
		try {
			if(user == null) {
				return DriverManager.getConnection(jdbcUrl);
			} else {
				return DriverManager.getConnection(jdbcUrl, user, password);
			}
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
