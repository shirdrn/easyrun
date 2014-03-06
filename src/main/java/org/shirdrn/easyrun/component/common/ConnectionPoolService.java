package org.shirdrn.easyrun.component.common;

import java.sql.Connection;

/**
 * Connection pool service protocol.
 * 
 * @author Shirdrn
 */
public interface ConnectionPoolService extends PoolService {
	
	Connection getConnection();
	void release(Connection conn);
	
}
