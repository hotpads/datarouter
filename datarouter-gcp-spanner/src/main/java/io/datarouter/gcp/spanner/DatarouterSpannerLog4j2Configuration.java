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
package io.datarouter.gcp.spanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import io.datarouter.gcp.spanner.ddl.SpannerDatabaseCreator;
import io.datarouter.gcp.spanner.ddl.SpannerSingleTableSchemaUpdateService;
import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.DatarouterLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.util.Require;

public class DatarouterSpannerLog4j2Configuration extends BaseLog4j2Configuration{

	private static boolean hasRun = false;

	public DatarouterSpannerLog4j2Configuration(){
		registerParent(DatarouterLog4j2Configuration.class);

		Appender schemaUpdateAppender = Log4j2Configurator.createConsoleAppender(
				"SpannerSchemaUpdate",
				Target.SYSTEM_OUT,
				"%msg%n");
		addAppender(schemaUpdateAppender);
		addLoggerConfig(SpannerSingleTableSchemaUpdateService.class.getName(), Level.INFO, false, schemaUpdateAppender);
		addLoggerConfig(SpannerDatabaseCreator.class.getName(), Level.INFO, false, schemaUpdateAppender);

		hasRun = true;
	}

	public static void assertHasRun(){
		Require.isTrue(hasRun, DatarouterSpannerLog4j2Configuration.class.getSimpleName() + " was not run");
	}

}
