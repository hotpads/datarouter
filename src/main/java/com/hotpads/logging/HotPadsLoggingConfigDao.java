package com.hotpads.logging;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;

import com.hotpads.util.core.logging.LoggingConfigDao;

@Singleton
public class HotPadsLoggingConfigDao implements LoggingConfigDao{

	@Inject
	private LoggingConfigNodes loggingConfigNodes;

	@Override
	public void createAndputLoggerConfig(String name, Level level, boolean additive, Iterable<String> appendersRef){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive, appendersRef);
		loggingConfigNodes.getLoggerConfig().put(loggerConfig, null);
	}

	@Override
	public void deleteLoggerConfig(String name){
		loggingConfigNodes.getLoggerConfig().delete(new LoggerConfigKey(name), null);
	}

	@Override
	public void createAndputConsoleAppender(String name, String pattern, String targetStr){
		ConsoleAppender appender = new ConsoleAppender(name, pattern, targetStr);
		loggingConfigNodes.getConsoleAppender().put(appender, null);
	}

	@Override
	public void deleteConsoleAppender(String name){
		loggingConfigNodes.getConsoleAppender().delete(new ConsoleAppenderKey(name), null);
	}

	@Override
	public void createAndputFileAppender(String name, String pattern, String fileName){
		FileAppender appender = new FileAppender(name, pattern, fileName);
		loggingConfigNodes.getFileAppender().put(appender, null);
	}

	@Override
	public void deleteFileAppender(String name){
		loggingConfigNodes.getFileAppender().delete(new FileAppenderKey(name), null);
	}

}
