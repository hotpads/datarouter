package com.hotpads.logging;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Log4j2Configuration{

	private Appender out;
	private Appender err;

	public Log4j2Configuration(){
		String pattern = "SLF4J %d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n";
		Layout<? extends Serializable> layout = PatternLayout.newBuilder().withPattern(pattern).build();
		out = ConsoleAppender.createAppender(layout , null, "SYSTEM_OUT", "Console", null, null);
		err = ConsoleAppender.createAppender(layout, null, "SYSTEM_ERR", "Err Out", null, null);
	}

	public List<Appender> getAppenders(){
		LinkedList<Appender> appenders = new LinkedList<Appender>();
		appenders.add(out);
		appenders.add(err);
		return appenders;
	}

	public List<LoggerConfig> getLoggerConfigs(){
		LinkedList<LoggerConfig> loggerConfigs = new LinkedList<LoggerConfig>();
		LoggerConfig root = new LoggerConfig("", Level.WARN, true);//Indicate the root because blank name
		root.addAppender(out, null, null);
		loggerConfigs.add(root);
		LoggerConfig another = new LoggerConfig("com.hotpads.logging.another", Level.ERROR, false);
		another.addAppender(err, null, null);
		loggerConfigs.add(another);
		return loggerConfigs;
	}

}
