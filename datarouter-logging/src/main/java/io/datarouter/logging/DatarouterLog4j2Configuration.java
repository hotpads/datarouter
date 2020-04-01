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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

public final class DatarouterLog4j2Configuration extends BaseLog4j2Configuration{

	public static final String CONSOLE_APPENDER_NAME = "Console";

	public DatarouterLog4j2Configuration(){
		Appender out = Log4j2Configurator.createConsoleAppender(CONSOLE_APPENDER_NAME, Target.SYSTEM_OUT,
				defaultPattern);
		addAppender(out);
		addLoggerConfig("", Level.WARN, false, out); // Indicate the root logger because blank name
	}

}
