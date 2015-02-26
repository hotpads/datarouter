package com.hotpads.util.core.logging;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.hotpads.util.core.java.ReflectionTool;

public abstract class HotPadsLog4j2Configuration{

	private Map<String,Appender> appenders = new HashMap<>();
	private Map<String,LoggerConfig> loggerConfigs = new HashMap<>();

	public final Collection<Appender> getAppenders(){
		return appenders.values();
	}

	protected final void addAppender(Appender appender){
		if(appenders.containsKey(appender.getName())){
			throw new IllegalArgumentException("Duplicate appender declaration : " + appender.getName());
		}
		appenders.put(appender.getName(), appender);
	}

	public final Collection<LoggerConfig> getLoggerConfigs(){
		return loggerConfigs.values();
	}

	protected final void addLoggerConfig(String name, Level level, boolean additive, Appender... appenders){
		addLoggerConfig(name, level, additive, Arrays.asList(appenders));
	}
	private final void addLoggerConfig(String name, Level level, boolean additive, Iterable<Appender> appenders){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive);
		for(Appender appender : appenders){
			loggerConfig.addAppender(appender, null, null);
		}
		if(loggerConfigs.containsKey(loggerConfig.getName())){
			throw new IllegalArgumentException("Duplicate logger config declaration : " + loggerConfig.getName());
		}
		loggerConfigs.put(loggerConfig.getName(), loggerConfig);
	}

	protected void registerParent(Class<? extends HotPadsLog4j2Configuration> clazz){
		HotPadsLog4j2Configuration configuration = ReflectionTool.create(clazz);
		for(Appender appender : configuration.getAppenders()){
			addAppender(appender);
		}
		for(LoggerConfig loggerConfig : configuration.getLoggerConfigs()){
			addLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel(), loggerConfig.isAdditive(), loggerConfig
					.getAppenders().values());
		}
	}

}
