package com.hotpads.util.core.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

@Plugin(category = "ConfigurationFactory", name = "StartupConfigurationFactory")
@Order(10)
public class StartupConfigurationFactory extends ConfigurationFactory{

	public static final Collection<Appender> staticAppenders = new LinkedList<>();
	public static final Collection<LoggerConfig> staticLoggerConfigs = new LinkedList<>();

	@Override
	public String[] getSupportedTypes(){
		// LOGGER.setLevel(Level.ALL); // Enable this to debug logging config
		return new String[]{".hotpads"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		@SuppressWarnings("serial")
		Configuration configuration = new AbstractConfiguration(ConfigurationSource.NULL_SOURCE){};
		String fullyQualifiedClassName;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream()))){
			fullyQualifiedClassName = reader.readLine();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		LOGGER.info("Configuring log4j2 with the class : {}", fullyQualifiedClassName);

		HotPadsLog4j2Configuration log4j2Configuration;
		try{
			Class<? extends HotPadsLog4j2Configuration> configurationClass = Class.forName(fullyQualifiedClassName)
					.asSubclass(HotPadsLog4j2Configuration.class);
			log4j2Configuration = configurationClass.newInstance();
		}catch(InstantiationException | IllegalAccessException | ClassNotFoundException e){
			throw new RuntimeException(e);
		}
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
