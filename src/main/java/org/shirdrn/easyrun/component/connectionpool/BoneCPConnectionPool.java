package org.shirdrn.easyrun.component.connectionpool;

import java.io.IOException;
import java.sql.Connection;

import org.shirdrn.easyrun.component.common.AbstractConnectionPool;

public class BoneCPConnectionPool extends AbstractConnectionPool {

	public BoneCPConnectionPool() {
		this("bonecp.properties");
	}
	
	public BoneCPConnectionPool(String config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Connection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

}
