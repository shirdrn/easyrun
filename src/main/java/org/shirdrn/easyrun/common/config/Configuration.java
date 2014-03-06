package org.shirdrn.easyrun.common.config;

/**
 * A {@link Configuration} object encapsulates 2 type of configuration objects:
 * <ol>
 * <li>Read-only {@link ContextReadable} object, usually which is a global
 * configuration object, and modification operations are forbidden.</li>
 * <li>Writeable {@link ContextWriteable} object, which is permit to modify
 * freely as needed.</li>
 * </ol>
 * 
 * @author Shirdrn
 */
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
