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
package io.datarouter.loggerconfig.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterLoggingConfigPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final LoggerConfigPaths logging = branch(LoggerConfigPaths::new, "logging");
	}

	public static class LoggerConfigPaths extends PathNode{
		public final PathNode createLoggerConfig = leaf("createLoggerConfig");
		public final PathNode deleteAppender = leaf("deleteAppender");
		public final PathNode deleteLoggerConfig = leaf("deleteLoggerConfig");
		public final PathNode editConsoleAppender = leaf("editConsoleAppender");
		public final PathNode editFileAppender = leaf("editFileAppender");
		public final PathNode showForm = leaf("showForm");
		public final PathNode testLog = leaf("testLog");
		public final PathNode updateLoggerConfig = leaf("updateLoggerConfig");
	}

}
