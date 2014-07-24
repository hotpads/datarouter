package com.hotpads.logging;

import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

@Singleton
public class Log4j2Configurator{

	private LoggerContext ctx;
	private Configuration config;
	private LoggerConfig rootLoggerConfig;

	public Log4j2Configurator(){
		ctx = (LoggerContext)LogManager.getContext(false);
		config = ctx.getConfiguration();
		Logger root = LogManager.getRootLogger();
		rootLoggerConfig = config.getLoggerConfig(root.getName());
	}

	public void setRootLevel(Level level){
		rootLoggerConfig.setLevel(level);
		ctx.updateLoggers();
	}

	public void setLevel(String name, Level level){
		LoggerConfig loggerConfig = config.getLoggerConfig(name);
		if(!loggerConfig.getName().equals(name)){ //it is the parent that have been found
			boolean parentAddictive = loggerConfig.isAdditive();
			loggerConfig = new LoggerConfig(name, level, parentAddictive);
			config.addLogger(name, loggerConfig);
		} else {
			loggerConfig.setLevel(level);
		}
		ctx.updateLoggers();
	}

	public void setLevel(Class<?> clazz, Level level){
		setLevel(clazz.getName(), level);
	}
}
