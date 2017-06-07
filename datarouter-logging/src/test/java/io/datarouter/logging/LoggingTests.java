/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.DatarouterLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;

/*
 * The config for these tests to pass is in TestDatarouterLog4j2Configuration
 */
public class LoggingTests{
	private static final Logger logger = LoggerFactory.getLogger(LoggingTests.class);
	private static final File TEST_FILE = new File(TestDatarouterLog4j2Configuration.TEST_FILE_NAME);
	private static final String TEST_FILE_2_NAME = TestDatarouterLog4j2Configuration.TEST_FILE_NAME + "2";
	private static final File TEST_FILE_2 = new File(TEST_FILE_2_NAME);
	private static final String TEST_APPENDER_2_NAME = "testFile2";
	private static final String ERR_CONSOLE_APPENDER_NAME = "second-console-appender";

	private final Log4j2Configurator configurator = new Log4j2Configurator();

	@AfterClass
	public void after(){
		configurator.shutdown();
		TEST_FILE.delete();
		TEST_FILE_2.delete();
	}

	@Test
	public void test() throws IOException{
		logger.debug("hello");
		logger.debug("password");//excluded by filter

		configurator.updateOrCreateLoggerConfig(getClass().getPackage(), Level.OFF, false,
				TestDatarouterLog4j2Configuration.TEST_APPENDER_NAME);
		logger.debug("goodbye");//excluded because Level is OFF

		configurator.updateOrCreateLoggerConfig(getClass(), Level.ALL, false,
				TestDatarouterLog4j2Configuration.TEST_APPENDER_NAME);
		logger.debug("foo");

		configurator.updateOrCreateLoggerConfig(getClass().getName(), Level.ALL, false, (Iterable<String>)null);
		logger.debug("bar");

		configurator.updateOrCreateLoggerConfig(getClass(), Level.ALL, false, "appender-not-found");
		logger.debug("baz");//excluded because appender not found

		Assert.assertTrue(configurator.getConfigs().keySet().contains(getClass().getName()));
		configurator.deleteLoggerConfig(getClass().getName());
		logger.debug("demat");//excluded because Level is OFF (package rule)
		Assert.assertFalse(configurator.getConfigs().keySet().contains(getClass().getName()));

		try(BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE))){
			Assert.assertTrue(reader.readLine().endsWith("hello"));
			Assert.assertTrue(reader.readLine().endsWith("foo"));
			Assert.assertTrue(reader.readLine().endsWith("bar"));
			Assert.assertNull(reader.readLine());
		}


		Assert.assertNull(configurator.getAppender(TEST_APPENDER_2_NAME));
		configurator.addFileAppender(TEST_APPENDER_2_NAME, TEST_FILE_2_NAME, BaseLog4j2Configuration.defaultPattern);
		Assert.assertNotNull(configurator.getAppender(TEST_APPENDER_2_NAME));

		configurator.updateOrCreateLoggerConfig(getClass(), Level.ALL, false, TEST_APPENDER_2_NAME);
		logger.warn("degemer");

		configurator.addConsoleAppender("second-console-appender", Target.SYSTEM_ERR,
				BaseLog4j2Configuration.defaultPattern);
		configurator.updateOrCreateLoggerConfig(getClass(), Level.ALL, false, ERR_CONSOLE_APPENDER_NAME);
		logger.warn("ar");//going to err console

		try(BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_2))){
			Assert.assertTrue(reader.readLine().endsWith("degemer"));
			Assert.assertNull(reader.readLine());
		}

		configurator.deleteAppender(TEST_APPENDER_2_NAME);
		Assert.assertNull(configurator.getAppender(TEST_APPENDER_2_NAME));

		Assert.assertTrue(configurator.getAppenders().keySet().contains(
				TestDatarouterLog4j2Configuration.TEST_APPENDER_NAME));
		Assert.assertTrue(configurator.getAppenders().keySet().contains(
				DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME));

		Assert.assertEquals(configurator.getRootLoggerConfig().getName(), "");
	}

}