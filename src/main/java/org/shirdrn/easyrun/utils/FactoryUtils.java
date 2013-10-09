package org.shirdrn.easyrun.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.shirdrn.easyrun.common.ObjectFactory;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.config.ContextReadable;
import org.shirdrn.easyrun.config.ContextWriteable;
import org.shirdrn.easyrun.config.PropertiesConfiguration;

public class FactoryUtils {

	private final static Map<String, ObjectFactory<?, ?>> INSTANCES = new HashMap<String, ObjectFactory<?, ?>>(0);
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

	public static ObjectFactory<?, ?> getFactory(String className) {
		ObjectFactory<?, ?> instance = (ObjectFactory<?, ?>) INSTANCES.get(className);
		lock.lock();
		try {
			if(instance == null) {
				instance = (ObjectFactory<?, ?>) ReflectionUtils.getInstance(className);
				INSTANCES.put(className, (ObjectFactory<?, ?>) instance);
			}
		} finally {
			lock.unlock();
		}
		return instance;
	}
	
	public static Configuration getDefaultConfiguration() {
		return CONFIGURATION;
	}
	
	public static void closeAll() {
		for(Entry<String, ObjectFactory<?, ?>> factory : INSTANCES.entrySet()) {
			factory.getValue().closeAll();
		}
	}
	
}
