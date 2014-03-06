package org.shirdrn.easyrun.common;

import java.util.Map.Entry;

/**
 * Manage versatile factory instances, and includes
 * producing objects, validating configurations, closing
 * closeable  objects, etc.
 * 
 * @author Shirdrn
 * @param <K>
 * @param <V>
 */
public interface ObjectFactory<K, V> extends Iterable<Entry<K, V>>{
	V get(K key);
	void closeAll();
	void close(V value);
	int count();
}
