package com.hotpads;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.logging.ConsoleAppender;
import com.hotpads.logging.FileAppender;
import com.hotpads.logging.LoggerConfig;
import com.hotpads.logging.LoggingConfigNodes;
import com.hotpads.util.core.logging.Log4j2Configurator;

@Singleton
public class ConfigLoggingListener implements HotPadsWebAppListener{

	@Inject
	private LoggingConfigNodes loggingConfigNodes;
	@Inject
	private Log4j2Configurator log4j2Configurator;

	@Override
	public void onStartUp(){
		Iterable<ConsoleAppender> consoleAppenders = loggingConfigNodes.getConsoleAppender().scan(null, null);
		for(ConsoleAppender consoleAppender : consoleAppenders){
			log4j2Configurator.addConsoleAppender(consoleAppender.getName(), consoleAppender.getTarget(),
					consoleAppender.getLayout());
		}
		Iterable<FileAppender> fileAppenders = loggingConfigNodes.getFileAppender().scan(null, null);
		for(FileAppender fileAppender : fileAppenders){
			log4j2Configurator.addFileAppender(fileAppender.getName(), fileAppender.getFileName(), fileAppender.getLayout());
		}
		Iterable<LoggerConfig> loggerConfigs = loggingConfigNodes.getLoggerConfig().scan(null, null);
		for(LoggerConfig loggerConfig : loggerConfigs){
			log4j2Configurator.updateOrCreateLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel().getLevel(),
					loggerConfig.getAdditive(), loggerConfig.getAppendersRef());
		}
	}

	@Override
	public void onShutDown(){}

}
