package com.hotpads.util.core.logging;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.status.StatusLogger;

import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.java.ReflectionTool;

@Plugin(category = "ConfigurationFactory", name = "StartupConfigurationFactory")
@Order(10)
public class StartupConfigurationFactory extends ConfigurationFactory{

	/**
	 * One logger to rule them all, one logger to find them,
	 * One logger to bring them all and in the darkness bind them.
	 */
	private static final StatusLogger LOGGER = StatusLogger.getLogger();

	public static final Collection<Appender> staticAppenders = new LinkedList<>();
	public static final Collection<LoggerConfig> staticLoggerConfigs = new LinkedList<>();

	@Override
	public String[] getSupportedTypes(){
		// LOGGER.setLevel(Level.ALL); // Enable this to debug logging config
		return new String[]{".hotpads"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		Configuration configuration = new AbstractConfiguration(ConfigurationSource.NULL_SOURCE){};
		String fullyQualifiedClassName = ReaderTool.accumulateStringAndClose(source.getInputStream()).toString();
		LOGGER.info("Configuring log4j2 with the class : {}", fullyQualifiedClassName);
		Object object = ReflectionTool.create(fullyQualifiedClassName);
		HotPadsLog4j2Configuration log4j2Configuration = (HotPadsLog4j2Configuration)object;
		for(Appender appender : log4j2Configuration.getAppenders()){
			configuration.addAppender(appender);
			staticAppenders.add(appender);
		}
		for(LoggerConfig loggerConfig : log4j2Configuration.getLoggerConfigs()){
			configuration.addLogger(loggerConfig.getName(), loggerConfig);
			staticLoggerConfigs.add(loggerConfig);
		}
		LOGGER.info("HotPadsConfig initiated");
		return configuration;
	}

}
