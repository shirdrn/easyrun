package org.shirdrn.easyrun.component.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Connection pool abstraction, which has a {@code dataSource} member,
 * can obtain a {@link Connection} object from the {@code dataSource}.
 * 
 * @author Shirdrn
 */
public abstract class AbstractConnectionPool extends AbstractPool implements ConnectionPoolService{

	public AbstractConnectionPool(String config) {
		super(config);
	}

	protected DataSource dataSource;
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	@Override
	public void release(Connection conn) {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
