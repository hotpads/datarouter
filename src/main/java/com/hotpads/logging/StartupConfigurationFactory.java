package com.hotpads.logging;

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

	@Override
	public String[] getSupportedTypes(){
		return new String[]{".hotpads"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		System.out.println("HPConfig initiated");
		Configuration configuration = new AbstractConfiguration(ConfigurationSource.NULL_SOURCE){};
		Log4j2Configuration log4j2Configuration = new Log4j2Configuration();
		for(Appender appender : log4j2Configuration.getAppenders()){
			configuration.addAppender(appender);
		}
		for(LoggerConfig loggerConfig : log4j2Configuration.getLoggerConfigs()){
			configuration.addLogger(loggerConfig.getName(), loggerConfig);
		}
		return configuration;
	}

}
