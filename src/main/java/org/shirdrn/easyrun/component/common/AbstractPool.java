package org.shirdrn.easyrun.component.common;

import javax.sql.DataSource;

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
