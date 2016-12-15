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
