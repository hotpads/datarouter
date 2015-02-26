package com.hotpads.logging;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;

import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.logging.databean.ConsoleAppender;
import com.hotpads.logging.databean.ConsoleAppenderKey;
import com.hotpads.logging.databean.FileAppender;
import com.hotpads.logging.databean.FileAppenderKey;
import com.hotpads.logging.databean.LoggerConfig;
import com.hotpads.logging.databean.LoggerConfigKey;
import com.hotpads.util.core.logging.LoggingConfigDao;

@Singleton
public class HotPadsLoggingConfigDao implements LoggingConfigDao{

	@Inject
	private LoggingConfigNodes loggingConfigNodes;

	@Override
	public void createAndputLoggerConfig(String name, Level level, boolean additive, List<String> appendersRef){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive, appendersRef);
		loggingConfigNodes.getLoggerConfig().put(loggerConfig, null);
	}

	@Override
	public void deleteLoggerConfig(String name){
		loggingConfigNodes.getLoggerConfig().delete(new LoggerConfigKey(name), null);
	}

	@Override
	public void createAndPutConsoleAppender(String name, String pattern, String targetStr){
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

	public LoggingConfig loadConfig(){
		Iterable<ConsoleAppender> consoleAppenders = loggingConfigNodes.getConsoleAppender().scan(null, null);
		Iterable<FileAppender> fileAppenders = loggingConfigNodes.getFileAppender().scan(null, null);
		Iterable<LoggerConfig> loggerConfigs = loggingConfigNodes.getLoggerConfig().scan(null, null);
		return new LoggingConfig(
				DrListTool.createArrayList(consoleAppenders),
				DrListTool.createArrayList(fileAppenders),
				DrListTool.createArrayList(loggerConfigs));
	}

}
