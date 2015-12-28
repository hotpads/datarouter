package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.Log4j2Configurator;

public final class TestDatarouterLog4j2Configuration extends HotPadsLog4j2Configuration{

	public static final String
			TEST_FILE_NAME = "/hotpads/tests/logging/testFile.log",
			CONSOLE_ERR_REF = "Console err";

	public TestDatarouterLog4j2Configuration(){
		registerParent(DatarouterLog4j2Configuration.class);

		Appender err = Log4j2Configurator.createConsoleAppender(CONSOLE_ERR_REF, ConsoleAppender.Target.SYSTEM_ERR
				.name(), defaultPattern);
		Appender testFile = Log4j2Configurator.createFileAppender("testFile", TEST_FILE_NAME, defaultPattern);

		addAppender(err);
		addAppender(testFile);

		addLoggerConfig("com.hotpads.logging.apackage", Level.TRACE, false, err);
		addLoggerConfig("com.hotpads.logging.another.Class4", Level.TRACE, false, testFile);
	}

}
