package org.shirdrn.easyrun.component.connectionpool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.shirdrn.easyrun.component.common.AbstractConnectionPool;

import com.alibaba.druid.pool.DruidDataSourceFactory;

/**
 * Druid connection pool component.
 * 
 * @author Shirdrn
 */
public class DruidConnectionPool extends AbstractConnectionPool {

	public DruidConnectionPool() {
		this("druid.properties");
	}
	
	public DruidConnectionPool(String config) {
		super("druid.properties");
		Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream(config));
			dataSource = DruidDataSourceFactory.createDataSource(properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
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
