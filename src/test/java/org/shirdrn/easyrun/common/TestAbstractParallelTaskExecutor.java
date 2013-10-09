package org.shirdrn.easyrun.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestAbstractParallelTaskExecutor {

	private static final Log LOG = LogFactory.getLog(TestAbstractParallelTaskExecutor.class);
	TaskExecutor<? extends ExecutionResult> executor;
	Configuration config;
	
	@Before
	public void initialize() {
		config = FactoryUtils.getDefaultConfiguration();
	}
	
	@Test
	public void testNormal() {
		executor = new AbstractParallelTaskExecutor<Entry<Integer,List<String>>>() {
			@Override
			public Iterator<Entry<Integer,List<String>>> iterator() {
				MyKeyValuePairs pairs = new MyKeyValuePairs();
				for(int i=0; i<100; i+=3) {
					pairs.put(i, Arrays.asList("Adam", "Allen"));
					pairs.put(i+1, Arrays.asList("Bosh", "Bob", "Bill"));
					pairs.put(i+2, Arrays.asList("Charlie", "Clan"));
				}
				return pairs.entrySet().iterator();
			}

			@Override
			protected void process(Entry<Integer, List<String>> element) throws Exception {
				LOG.info("Print: " + element.getKey() + ". " + element.getValue().toString());			
			}
		};
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		LOG.info(result.toString());
	}
	
	@Test
	public void testRunWhenFailure() {
		boolean isTterminateWhenFailure = false;
		executor = getExecutor(isTterminateWhenFailure);
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		LOG.info(result.toString());
	}
	
	@Test
	public void testStopWhenFailure() {
		boolean isTterminateWhenFailure = true;
		executor = getExecutor(isTterminateWhenFailure);
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		LOG.info(result.toString());
	}

	private TaskExecutor<? extends ExecutionResult> getExecutor(final boolean isTterminateWhenFailure) {
		TaskExecutor<? extends ExecutionResult> executor = new PrintTaskExecutor();
		return executor;
	}
	
	@After
	public void destroy() {
		FactoryUtils.closeAll();
	}

}
