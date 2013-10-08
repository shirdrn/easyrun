package org.shirdrn.easyrun.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.shirdrn.easyrun.config.Configuration;
import org.shirdrn.easyrun.utils.FactoryUtils;

public class TestAbstractSequentialTaskExecutor {

	TaskExecutor<? extends ExecutionResult> executor;
	Configuration config;
	
	class MySequentialTaskExecutor extends AbstractSequentialTaskExecutor<Entry<Integer,List<String>>> {

		@Override
		public Iterator<Entry<Integer,List<String>>> iterator() {
			MyKeyValuePairs pairs = new MyKeyValuePairs();
			for(int i=0; i<1; i+=3) {
				pairs.put(i, Arrays.asList("Adam", "Allen"));
				pairs.put(i+1, Arrays.asList("Bosh", "Bob", "Bill"));
				pairs.put(i+2, Arrays.asList("Charlie", "Clan"));
			}
			return pairs.entrySet().iterator();
		}

		@Override
		protected void process(Entry<Integer, List<String>> element) throws Exception {
			System.out.println(element.getKey() + ". " + element.getValue().toString());			
		}

	}
	
	class MyKeyValuePairs extends HashMap<Integer, List<String>> {

		private static final long serialVersionUID = 5888925024425801257L;
		
	}
	
	@Before
	public void initialize() {
		config = FactoryUtils.getDefaultConfiguration();
		executor = new MySequentialTaskExecutor();
	}
	
	@Test
	public void testNormal() {
		executor.configure(config);
		executor.execute();
		ExecutionResult result = executor.getResult();
		System.out.println(result.toString());
	}
	

}
