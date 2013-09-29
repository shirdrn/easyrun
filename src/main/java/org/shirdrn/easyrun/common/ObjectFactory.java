package org.shirdrn.easyrun.common;

import java.io.Closeable;

public interface ObjectFactory<K, V extends Closeable> {

	V get(K key);
	void close(V value);
}
