package org.shirdrn.easyrun.component.common;

import java.sql.Connection;

public interface ConnectionPoolService extends PoolService {
	
	Connection getConnection();
	void release(Connection conn);
	
}
