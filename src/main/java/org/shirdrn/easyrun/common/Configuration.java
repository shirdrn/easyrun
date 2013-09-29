package org.shirdrn.easyrun.common;

import org.shirdrn.easyrun.utils.PropertiesConfiguration;

public class Configuration {
	
	private final ContextReadable readableContext;
	private final ContextWriteable writeableContext;
	
	public Configuration(ContextReadable rContext) {
		readableContext = rContext;
		writeableContext = new PropertiesConfiguration();
	}

	public ContextWriteable getWContext() {
		return writeableContext;
	}
	
	public ContextReadable getRContext() {
		return readableContext;
	}
}
