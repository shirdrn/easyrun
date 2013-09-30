package org.shirdrn.easyrun.component.threadpool;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.shirdrn.easyrun.common.ThreadPoolService;

public class ManagedThreadPool extends ThreadPoolExecutor implements ThreadPoolService {

	public ManagedThreadPool(int corePoolSize, int maximumPoolSize, 
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, 
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	@Override
	public void close() throws IOException {
		super.shutdown();		
	}

}
