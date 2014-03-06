package org.shirdrn.easyrun.component.common;

/**
 * Pool abstraction.
 * 
 * @author Shirdrn
 */
public class AbstractPool {

	protected final String config;
	
	public AbstractPool(String config) {
		super();
		this.config = config;
	}

	public String getConfig() {
		return config;
	}

}
