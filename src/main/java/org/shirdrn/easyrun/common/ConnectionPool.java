package org.shirdrn.easyrun.common;

import java.sql.Connection;

public interface ConnectionPool extends PoolService {
	
	Connection getConnection();
	void release(Connection conn);
	
}
