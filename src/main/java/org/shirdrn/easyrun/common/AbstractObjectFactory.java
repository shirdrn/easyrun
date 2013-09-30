package org.shirdrn.easyrun.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractObjectFactory<K, V extends Closeable> implements ObjectFactory<K, V> {

	protected final Map<K, V> cache = new HashMap<K, V>();
	
	public AbstractObjectFactory() {
		super();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
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
		});
	}

}
