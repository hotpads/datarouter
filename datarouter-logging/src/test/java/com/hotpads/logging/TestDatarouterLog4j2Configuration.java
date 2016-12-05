package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.filter.RegexFilter;

import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.Log4j2Configurator;

public final class TestDatarouterLog4j2Configuration extends HotPadsLog4j2Configuration{

	public static final String TEST_FILE_NAME = "/hotpads/tests/logging/testFile.log";
	public static final String TEST_APPENDER_NAME = "testFile";

	public TestDatarouterLog4j2Configuration(){
		registerParent(DatarouterLog4j2Configuration.class);
		registerParent(TestDatarouterParentLog4j2Configuration.class);

		Appender consoleAppender = getAppender(DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME);
		addLoggerConfig("com", Level.OFF, false, consoleAppender);
	}

	public static final class TestDatarouterParentLog4j2Configuration extends HotPadsLog4j2Configuration{

		public TestDatarouterParentLog4j2Configuration() throws IllegalAccessException{
			addFilter(RegexFilter.createFilter(".*password.*", null, true, Result.DENY, Result.NEUTRAL));

			FileAppender testFileAppender = Log4j2Configurator.createFileAppender(TEST_APPENDER_NAME, TEST_FILE_NAME,
					defaultPattern);
			addAppender(testFileAppender);
			addLoggerConfig(getClass().getPackage().getName(), Level.ALL, false, testFileAppender);
		}

	}

}
