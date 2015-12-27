package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.Log4j2Configurator;

public final class DatarouterLog4j2Configuration extends HotPadsLog4j2Configuration{

	public static final String CONSOLE_APPENDER_NAME = "Console";

	public DatarouterLog4j2Configuration(){
		Appender out = Log4j2Configurator.createConsoleAppender(CONSOLE_APPENDER_NAME, ConsoleAppender.Target.SYSTEM_OUT
				.name(), defaultPattern);

		addAppender(out);

		addLoggerConfig("", Level.WARN, false, out); //Indicate the root logger because blank name
		addLoggerConfig("com.hotpads", Level.WARN, false, out);
	}

}
