package org.shirdrn.easyrun.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MySequentialTaskExecutor extends AbstractSequentialTaskExecutor<Entry<Integer,List<String>>> {

	private static final Log LOG = LogFactory.getLog(MySequentialTaskExecutor.class);
	
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
		LOG.info(element.getKey() + " => " + element.getValue().toString());			
	}

}