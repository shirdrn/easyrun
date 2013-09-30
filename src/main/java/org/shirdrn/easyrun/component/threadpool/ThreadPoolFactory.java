package org.shirdrn.easyrun.component.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.shirdrn.easyrun.common.AbstractObjectFactory;
import org.shirdrn.easyrun.common.ThreadPoolService;
import org.shirdrn.easyrun.config.ContextReadable;

public class ThreadPoolFactory extends AbstractObjectFactory<ContextReadable, ThreadPoolService> {

	@Override
	public ThreadPoolService get(ContextReadable key) {
		ThreadPoolService pool = cache.get(key);
		if(pool == null) {
			String name = key.get("component.thread.pool.name", "EASYRUN");
			int nThreads = key.getInt("component.thread.pool.worker.count", 1);
			int workQSize = 2 * nThreads;
			BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(workQSize);
			pool = new ManagedThreadPool(nThreads, nThreads,
					0L, TimeUnit.MILLISECONDS, q, new DefaultThreadFactory(name), 
					new ScheduleAgainPolicy(workQSize));
			cache.put(key, pool);
		}
		return pool;
	}

	@Override
	public void close(ThreadPoolService value) {
		value.shutdown();		
	}
	
}
