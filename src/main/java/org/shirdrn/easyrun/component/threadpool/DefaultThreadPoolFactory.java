package org.shirdrn.easyrun.component.threadpool;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.shirdrn.easyrun.common.ThreadPoolFactory;
import org.shirdrn.easyrun.common.ThreadPoolService;
import org.shirdrn.easyrun.common.config.ContextReadable;

public class DefaultThreadPoolFactory extends ThreadPoolFactory {

	public DefaultThreadPoolFactory() {
		super();
	}
	
	@Override
	public ThreadPoolService get(ContextReadable key) {
		ThreadPoolService pool = super.get(key);
		if(pool == null) {
			pool = createAndCachePool(key);
		} else {
			if(pool.isShutdown() || pool.isTerminated()) {
				pool = createAndCachePool(key);
			}
		}
		return pool;
	}

	private ThreadPoolService createAndCachePool(ContextReadable key) {
		ThreadPoolService pool = null;
		String name = key.get("component.thread.pool.name", "EASYRUN");
		int nThreads = key.getInt("component.thread.pool.worker.count", 1);
		int workQSize = 2 * nThreads;
		BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(workQSize);
		pool = new ManagedThreadPool(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS, q, new NamedThreadFactory(name), 
				new ScheduleAgainPolicy(workQSize));
		super.put(key, pool);
		return pool;
	}
	
	@Override
	public void closeAll() {
		Iterator<Entry<ContextReadable, ThreadPoolService>> iter = super.iterator();
		while(iter.hasNext()) {
			ThreadPoolService pool = iter.next().getValue();
			if(!pool.isShutdown()) {
				pool.shutdown();
			}
		}	
	}

	@Override
	public void close(ThreadPoolService value) {
		if(!value.isShutdown()) {
			value.shutdown();
		}
	}

}
