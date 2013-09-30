package org.shirdrn.easyrun.config;


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
