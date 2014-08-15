package com.hotpads.logging;

import javax.inject.Singleton;

import org.apache.logging.log4j.core.Appender;

import com.hotpads.logging.databean.ConsoleAppender;
import com.hotpads.logging.databean.FileAppender;
import com.hotpads.logging.databean.LoggerConfig;
import com.hotpads.util.core.logging.Log4j2Configurator;
import com.hotpads.util.core.logging.StartupConfigurationFactory;

@Singleton
public class HotPadsLog4j2Configurator extends Log4j2Configurator{

	public void applyConfig(LoggingConfig config){
		removeAllAppender();
		for(ConsoleAppender consoleAppender : config.getConsoleAppenders()){
			addConsoleAppender(consoleAppender.getName(), consoleAppender.getTarget(), consoleAppender.getLayout());
		}
		for(FileAppender fileAppender : config.getFileAppenders()){
			addFileAppender(fileAppender.getName(), fileAppender.getFileName(), fileAppender.getLayout());
		}
		
		removeAllConfigLogger();
		for(LoggerConfig loggerConfig : config.getLoggerConfigs()){
			updateOrCreateLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel().getLevel(), loggerConfig
					.getAdditive(), loggerConfig.getAppendersRef());
		}
	}

	private void removeAllConfigLogger(){
		for(org.apache.logging.log4j.core.config.LoggerConfig config : getConfigs().values()){
			if(!StartupConfigurationFactory.staticLoggerConfigs.contains(config)){
				deleteLoggerConfig(config.getName());
			}
		}
	}

	private void removeAllAppender(){
		for(Appender appender : getAppenders().values()){
			if(!StartupConfigurationFactory.staticAppenders.contains(appender)){
				deleteAppender(appender.getName());
			}
		}
	}

}
