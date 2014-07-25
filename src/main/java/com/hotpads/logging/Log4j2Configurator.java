package com.hotpads.logging;

import java.util.Map;

import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
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

	public void updateOrCreateLoggerConfig(String name, Level level, boolean additive){
		LoggerConfig loggerConfig = config.getLoggerConfig(name);
		if(!loggerConfig.getName().equals(name)){ // it is the parent that have been found
			loggerConfig = new LoggerConfig(name, level, additive);
			config.addLogger(name, loggerConfig);
		}else{
			loggerConfig.setLevel(level);
			loggerConfig.setAdditive(additive);
		}
		ctx.updateLoggers();
	}

	public void updateOrCreateLoggerConfig(Class<?> clazz, Level level, boolean additive){
		updateOrCreateLoggerConfig(clazz.getName(), level, additive);
	}

	public Map<String,LoggerConfig> getConfigs(){
		return config.getLoggers();
	}

	public Map<String,Appender> getAppenders(){
		return config.getAppenders();
	}

	public LoggerConfig getRootLoggerConfig(){
		return rootLoggerConfig;
	}

	public Level getRootLevel(){
		return rootLoggerConfig.getLevel();
	}

}
