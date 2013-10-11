package org.shirdrn.easyrun.component.connectionpool;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.shirdrn.easyrun.common.ConnectionPoolFactory;
import org.shirdrn.easyrun.common.ConnectionPoolService;
import org.shirdrn.easyrun.utils.ReflectionUtils;

public class DefaultConnectionPoolFactory extends ConnectionPoolFactory {

	public DefaultConnectionPoolFactory() {
		super();
	}
	
	@Override
	public ConnectionPoolService get(String key) {
		ConnectionPoolService pool = super.get(key);
		if(pool == null) {
			pool = (ConnectionPoolService) ReflectionUtils.getInstance(key);
			super.put(key, pool);
		}
		return pool;
	}
	
	@Override
	public void closeAll() {
		Iterator<Entry<String, ConnectionPoolService>> iter = super.iterator();
		while(iter.hasNext()) {
			ConnectionPoolService pool = iter.next().getValue();
			close(pool);
		}	
	}

	@Override
	public void close(ConnectionPoolService value) {
		try {
			value.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
