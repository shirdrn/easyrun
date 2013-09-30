package org.shirdrn.easyrun.common;


public interface ObjectFactory<K, V> {

	V get(K key);
	void close(V value);
}
