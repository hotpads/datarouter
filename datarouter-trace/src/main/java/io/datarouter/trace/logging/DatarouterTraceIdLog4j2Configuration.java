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
package io.datarouter.trace.logging;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.DatarouterLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;

/**
 * This class overrides the "Console" logger defined by DatarouterLog4j2Configuration with a pattern which reads
 * the traceId from the ThreadContext to link logs together within a single request as well as linking them to the
 * distributed trace.
  */
public final class DatarouterTraceIdLog4j2Configuration extends BaseLog4j2Configuration{

	public static final String TRACE_ID_CONSOLE_PATTERN = "%d %-5level [%t] "
			+ BaseLog4j2Configuration.SERVLET_NAME_OR_PID
			+ " %logger{36}:%line - [%X{traceId}] %msg%n%rEx";

	public DatarouterTraceIdLog4j2Configuration(){
		Appender traceIdConsoleAppender = Log4j2Configurator.createConsoleAppender(
				DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME,
				Target.SYSTEM_OUT,
				DatarouterTraceIdLog4j2Configuration.TRACE_ID_CONSOLE_PATTERN);
		addAppender(traceIdConsoleAppender);
	}

}
