package org.shirdrn.easyrun.config;


public class Configuration {
	
	private final ContextReadable readableContext;
	private ContextWriteable writeableContext;
	
	public Configuration(ContextReadable rContext) {
		readableContext = rContext;
	}

	public void setWContext(ContextWriteable wContext) {
		this.writeableContext = wContext;
	}
	
	public ContextWriteable getWContext() {
		return writeableContext;
	}
	
	public ContextReadable getRContext() {
		return readableContext;
	}

}
