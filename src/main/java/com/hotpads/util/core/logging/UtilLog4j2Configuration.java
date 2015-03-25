package com.hotpads.util.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

public final class UtilLog4j2Configuration extends HotPadsLog4j2Configuration{

	public static final String testFileName = "/hotpads/tests/logging/testFile.log";

	public static final String defaultPattern = "%d %-5level [%t] %logger{36}:%line - %msg%n%rEx";

	public UtilLog4j2Configuration(){
		Appender out = Log4j2Configurator.createConsoleAppender("Console", ConsoleAppender.Target.SYSTEM_OUT.name(), defaultPattern);

		addAppender(out);

		addLoggerConfig("", Level.WARN, false, out); //Indicate the root logger because blank name
		addLoggerConfig("com.hotpads", Level.WARN, false, out);
	}

}
