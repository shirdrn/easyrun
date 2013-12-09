package org.shirdrn.easyrun.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.shirdrn.easyrun.common.ObjectFactory;
import org.shirdrn.easyrun.common.config.Configuration;
import org.shirdrn.easyrun.common.config.ContextReadable;
import org.shirdrn.easyrun.common.config.ContextWriteable;
import org.shirdrn.easyrun.common.config.PropertiesConfiguration;

/**
 * Utility for managing registered factory instances.
 * @author Shirdrn
 */
public class FactoryUtils {

	private static final Map<String, ObjectFactory<?, ?>> INSTANCES = new HashMap<String, ObjectFactory<?, ?>>(0);
	private static final Map<Class<?>, Set<String>> CLASSES = new HashMap<Class<?>, Set<String>>(0);
	private static final Lock lock = new ReentrantLock();
	private static Configuration CONFIGURATION = null;
	static {
		// load default configuration: config.properties
		Class<? extends ContextReadable> contextClass = PropertiesConfiguration.class;
		if(CONFIGURATION == null) {
			ContextReadable rContext = ReflectionUtils.getInstance(contextClass, "config.properties");
			CONFIGURATION = new Configuration(rContext);
			ContextWriteable wContext = (ContextWriteable) ReflectionUtils.getInstance(contextClass);
			CONFIGURATION.setWContext(wContext);
		}
	}

	/**
	 * Get a instance of class <code>baseClazz</code> 
	 * from class name <code>className</code>.
	 * @param className
	 * @param baseClazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFactory(String className, Class<T> baseClazz) {
		register(baseClazz, className);
		return (T) INSTANCES.get(className);
	}
	
	private static <T> void register(Class<T> baseClazz, String className) {
		Set<String> cachedClasses = CLASSES.get(baseClazz);
		lock.lock();
		try {
			if(cachedClasses == null) {
				cachedClasses = new HashSet<String>();
				T instance = ReflectionUtils.getInstance(className, baseClazz, null);
				cachedClasses.add(className);
				CLASSES.put(baseClazz, cachedClasses);
				INSTANCES.put(className, (ObjectFactory<?, ?>) instance);
			} else {
				if(!cachedClasses.contains(className)) {
					cachedClasses.add(className);
					if(!INSTANCES.containsKey(className)) {
						T instance = ReflectionUtils.getInstance(className, baseClazz, null);
						INSTANCES.put(className, (ObjectFactory<?, ?>) instance);
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Get default {@link Configuration} object.
	 * @return
	 */
	public static Configuration getDefaultConfiguration() {
		return CONFIGURATION;
	}
	
	/**
	 * Close a specified {@link ObjectFactory} instance.
	 * @param factory
	 */
	public static void close(ObjectFactory<?, ?> factory) {
		if(factory != null) {
			factory.closeAll();
		}
	}
	
	/**
	 * Close all registered instances of {@link ObjectFactory}.
	 */
	public static void closeAll() {
		for(Entry<String, ObjectFactory<?, ?>> factory : INSTANCES.entrySet()) {
			factory.getValue().closeAll();
		}
	}
	
}
