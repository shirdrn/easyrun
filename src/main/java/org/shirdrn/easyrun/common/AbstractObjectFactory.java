package org.shirdrn.easyrun.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractObjectFactory<K, V extends Closeable> implements ObjectFactory<K, V> {

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
				iter.next().getValue().close();
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

	@Override
	public void put(K key, V value) {
		cache.put(key, value);
	}

}
