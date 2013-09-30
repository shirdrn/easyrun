package org.shirdrn.easyrun.component.connpool;

import java.io.IOException;

import org.shirdrn.easyrun.common.AbstractObjectFactory;
import org.shirdrn.easyrun.common.ConnectionPool;
import org.shirdrn.easyrun.utils.ReflectionUtils;

public class ConnectionPoolFactory extends AbstractObjectFactory<String, ConnectionPool> {

	@Override
	public ConnectionPool get(String key) {
		ConnectionPool pool = cache.get(key);
		if(pool == null) {
			pool = (ConnectionPool) ReflectionUtils.getInstance(key);
			cache.put(key, pool);
		}
		return pool;
	}

	@Override
	public void close(ConnectionPool value) {
		try {
			value.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
