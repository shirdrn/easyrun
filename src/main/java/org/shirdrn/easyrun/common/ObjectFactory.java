package org.shirdrn.easyrun.common;

import java.util.Map.Entry;


public interface ObjectFactory<K, V> extends Iterable<Entry<K, V>>{

	V get(K key);
	void closeAll();
	void close(V value);
	int count();
}
