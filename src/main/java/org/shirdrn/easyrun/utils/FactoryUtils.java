package org.shirdrn.easyrun.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.config.ContextReadable;
import org.shirdrn.easyrun.config.ContextWriteable;
import org.shirdrn.easyrun.config.PropertiesConfiguration;

public class FactoryUtils {

	private final static Map<String, Object> INSTANCES = new HashMap<String, Object>(0);
	private static Lock lock = new ReentrantLock();
	private static Configuration CONFIGURATION = null;
	static {
		Class<? extends ContextReadable> contextClass = PropertiesConfiguration.class;
		if(CONFIGURATION == null) {
			ContextReadable rContext = ReflectionUtils.getInstance(contextClass, "config.properties");
			CONFIGURATION = new Configuration(rContext);
			ContextWriteable wContext = (ContextWriteable) ReflectionUtils.getInstance(contextClass);
			CONFIGURATION.setWContext(wContext);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFactory(String className, Class<T> clazz) {
		T instance = (T) INSTANCES.get(className);
		lock.lock();
		try {
			if(instance == null) {
				instance = (T) ReflectionUtils.getInstance(className);
				INSTANCES.put(className, instance);
			}
		} finally {
			lock.unlock();
		}
		return instance;
	}
	
	public static Configuration getDefaultConfiguration() {
		return CONFIGURATION;
	}
	
}
