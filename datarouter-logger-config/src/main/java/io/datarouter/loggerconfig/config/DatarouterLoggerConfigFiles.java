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
package io.datarouter.loggerconfig.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.PathNode;

@Singleton
public class DatarouterLoggerConfigFiles extends FilesRoot{

	public final JspFiles jsp = branch(JspFiles::new, "jsp");

	public static class JspFiles extends PathNode{
		public final DatarouterFiles datarouter = branch(DatarouterFiles::new, "datarouter");
	}

	public static class DatarouterFiles extends PathNode{
		public final LoggerConfigFiles loggerConfig = branch(LoggerConfigFiles::new, "loggerConfig");
	}

	public static class LoggerConfigFiles extends PathNode{
		public final PathNode consoleAppenderJsp = leaf("consoleAppender.jsp");
		public final PathNode fileAppenderJsp = leaf("fileAppender.jsp");
		public final PathNode loggingJsp = leaf("logging.jsp");
	}

}
