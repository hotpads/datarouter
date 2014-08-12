package com.hotpads.logging;

import java.util.List;

import com.hotpads.logging.databean.ConsoleAppender;
import com.hotpads.logging.databean.FileAppender;
import com.hotpads.logging.databean.LoggerConfig;

public class LoggingConfig{

	public static String computeSignature(List<ConsoleAppender> consoles, List<FileAppender> files,
			List<LoggerConfig> loggers){
		StringBuilder builder = new StringBuilder();
		for(ConsoleAppender consoleAppender : consoles){
			builder.append(consoleAppender.getName());
			builder.append(consoleAppender.getLayout());
			builder.append(consoleAppender.getTarget());
		}
		for(FileAppender fileAppender : files){
			builder.append(fileAppender.getName());
			builder.append(fileAppender.getLayout());
			builder.append(fileAppender.getFileName());
		}
		for(LoggerConfig loggerConfig : loggers){
			builder.append(loggerConfig.getName());
			builder.append(loggerConfig.getLevel());
			builder.append(loggerConfig.getAdditive());
			builder.append(loggerConfig.getAppendersRef());
		}
		return builder.toString();
	}

	private List<ConsoleAppender> consoleAppenders;
	private List<FileAppender> fileAppenders;
	private List<LoggerConfig> loggerConfigs;
	private String signature;

	public LoggingConfig(List<ConsoleAppender> consoleAppenders, List<FileAppender> fileAppenders,
			List<LoggerConfig> loggerConfigs){
		this.consoleAppenders = consoleAppenders;
		this.fileAppenders = fileAppenders;
		this.loggerConfigs = loggerConfigs;
		this.signature = computeSignature(consoleAppenders, fileAppenders, loggerConfigs);
	}

	public List<ConsoleAppender> getConsoleAppenders(){
		return consoleAppenders;
	}
	public List<FileAppender> getFileAppenders(){
		return fileAppenders;
	}
	public List<LoggerConfig> getLoggerConfigs(){
		return loggerConfigs;
	}
	public String getSignature(){
		return signature;
	}

}
