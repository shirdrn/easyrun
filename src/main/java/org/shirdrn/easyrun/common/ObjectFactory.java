package org.shirdrn.easyrun.common;

import java.util.Map.Entry;


public interface ObjectFactory<K, V> extends Iterable<Entry<K, V>>{

	void put(K key, V value);
	V get(K key);
	void closeAll();
	void close(V value);
	int count();
}
