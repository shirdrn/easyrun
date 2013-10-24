package org.shirdrn.easyrun.component.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.common.config.ContextReadable;
import org.shirdrn.easyrun.common.config.PropertiesConfiguration;
import org.shirdrn.easyrun.component.common.AbstractPool;
import org.shirdrn.easyrun.component.common.ConnectionPoolService;

public final class JDBCConnectionPool extends AbstractPool implements ConnectionPoolService {
	
	private static final Log LOG = LogFactory.getLog(JDBCConnectionPool.class);
	private final ContextReadable context;
	private String jdbcUrl;
	private String user;
	private String password;
	
	public JDBCConnectionPool() {
		this("jdbc.properties");
	}
	
	public JDBCConnectionPool(String config) {
		super(config);
		this.context = new PropertiesConfiguration(config);
		String driverClass = context.get("jdbc.driverClass");
		try {
			Class.forName(driverClass);
			jdbcUrl = context.get("jdbc.jdbcUrl");
			user = context.get("jdbc.user");
			password = context.get("jdbc.password");
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
			e.printStackTrace();
		}	
	}

}
