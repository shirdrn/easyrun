package org.shirdrn.easyrun.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FactoryUtils {

	private final static Map<Class<?>, Object> CONTAINER = new HashMap<Class<?>, Object>(0);
	private static Lock lock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	public static <T> T getFactory(Class<T> clazz) {
		T instance = (T) CONTAINER.get(clazz);
		lock.lock();
		try {
			if(instance == null) {
				instance = ReflectionUtils.getInstance(clazz);
				CONTAINER.put(clazz, instance);
			}
		} finally {
			lock.unlock();
		}
		return instance;
	}
}
