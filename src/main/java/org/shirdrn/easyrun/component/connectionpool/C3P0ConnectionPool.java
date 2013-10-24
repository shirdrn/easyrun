package org.shirdrn.easyrun.component.connectionpool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.easyrun.component.common.AbstractConnectionPool;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0ConnectionPool extends AbstractConnectionPool {

	private static final Log LOG = LogFactory.getLog(C3P0ConnectionPool.class);
	
	public C3P0ConnectionPool() {
		this("c3p0.properties");
	}
	
	public C3P0ConnectionPool(String config) {
		super(config);
		dataSource = new ComboPooledDataSource(config);
		LOG.info("Create datasource: ds=" + dataSource);
	}

	@Override
	public void close() throws IOException {
		if(dataSource != null) {
			((ComboPooledDataSource) dataSource).close();
		}
	}

	@Override
	public synchronized final Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
