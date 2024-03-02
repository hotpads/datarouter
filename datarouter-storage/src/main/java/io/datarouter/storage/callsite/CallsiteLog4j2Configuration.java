/*
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
package io.datarouter.storage.callsite;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;

import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;

public final class CallsiteLog4j2Configuration extends BaseLog4j2Configuration{

	public CallsiteLog4j2Configuration(){
		String callsiteLogFile = CATALINA_OUT_DIR + "/callsite.log";
		Appender callsiteAppender = Log4j2Configurator.createFileAppender(
				"callsite",
				callsiteLogFile,
				"%d %-5level [%t] %logger{36}:%line - %msg%n%rEx");
		addAppender(callsiteAppender);
		addLoggerConfig(CallsiteRecorder.class.getName(), Level.TRACE, false, callsiteAppender);
	}

}
