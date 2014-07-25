package com.hotpads.handler.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class MConfig{

	private String name;
	private Level level;
	private Boolean aditive;

	public MConfig(LoggerConfig config){
		name = config.getName();
		level = config.getLevel();
		aditive = config.isAdditive();
	}

}
