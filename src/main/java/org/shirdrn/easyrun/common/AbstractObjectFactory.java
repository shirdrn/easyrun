package org.shirdrn.easyrun.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractObjectFactory<K, V extends Closeable> implements ObjectFactory<K, V> {

	private static final Log LOG = LogFactory.getLog(AbstractObjectFactory.class);
	private final Map<K, V> cache = new HashMap<K, V>();
	
	public AbstractObjectFactory() {
		super();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeAll();
			}
		});
	}

	@Override
	public void closeAll() {
		Iterator<Entry<K, V>> iter = 
				cache.entrySet().iterator();
		while(iter.hasNext()) {
			try {
				Entry<K, V> entry = iter.next();
				entry.getValue().close();
				LOG.info("Closed: key=" + entry.getKey() + ", value=" + entry.getValue());
				iter.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public int count() {
		return cache.size();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return cache.entrySet().iterator();
	}

	@Override
	public V get(K key) {
		return cache.get(key);
	}

	protected void put(K key, V value) {
		cache.put(key, value);
		LOG.info("Cached: key=" + key + ", value=" + value);
	}
	
	protected void remove(V value) {
		Iterator<Entry<K, V>> iter = iterator();
		while(iter.hasNext()) {
			if(value != null && value.equals(iter.next().getValue())) {
				iter.remove();break;
			}
		}
	}

}
