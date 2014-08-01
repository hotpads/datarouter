package com.hotpads.logging;

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

	private static final StatusLogger LOGGER = StatusLogger.getLogger();

	@Override
	public String[] getSupportedTypes(){
		return new String[]{".hotpads"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
//		LOGGER.setLevel(Level.TRACE);
		Configuration configuration = new AbstractConfiguration(ConfigurationSource.NULL_SOURCE){};
		String fullyQualifiedClassName = ReaderTool.accumulateStringAndClose(source.getInputStream()).toString();
		LOGGER.info("Configuring log4j2 with the class : {}", fullyQualifiedClassName);
		Object object = ReflectionTool.create(fullyQualifiedClassName);
		Log4j2Configuration log4j2Configuration = (Log4j2Configuration)object;
		for(Appender appender : log4j2Configuration.getAppenders()){
			configuration.addAppender(appender);
		}
		for(LoggerConfig loggerConfig : log4j2Configuration.getLoggerConfigs()){
			configuration.addLogger(loggerConfig.getName(), loggerConfig);
		}
		LOGGER.info("HotPadsConfig initiated");
		return configuration;
	}

}
