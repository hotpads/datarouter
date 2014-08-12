package com.hotpads.logging;

import javax.inject.Singleton;

import com.hotpads.logging.databean.ConsoleAppender;
import com.hotpads.logging.databean.FileAppender;
import com.hotpads.logging.databean.LoggerConfig;
import com.hotpads.util.core.logging.Log4j2Configurator;

@Singleton
public class HotPadsLog4j2Configurator extends Log4j2Configurator{

	public void appConfig(LoggingConfig config){
		for(ConsoleAppender consoleAppender : config.getConsoleAppenders()){
			addConsoleAppender(consoleAppender.getName(), consoleAppender.getTarget(), consoleAppender.getLayout());
		}
		for(FileAppender fileAppender : config.getFileAppenders()){
			addFileAppender(fileAppender.getName(), fileAppender.getFileName(), fileAppender.getLayout());
		}
		for(LoggerConfig loggerConfig : config.getLoggerConfigs()){
			updateOrCreateLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel().getLevel(), loggerConfig
					.getAdditive(), loggerConfig.getAppendersRef());
		}
	}

}
