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
package io.datarouter.exception.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterExceptionPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final ExceptionPaths exception = branch(ExceptionPaths::new, "exception");
		public final ErrorGeneratorPaths errorGenerator = branch(ErrorGeneratorPaths::new, "errorGenerator");
	}

	public static class ExceptionPaths extends PathNode{
		public final PathNode browse = leaf("browse");
		public final PathNode details = leaf("details");
		public final PathNode mute = leaf("mute");
		public final PathNode recordIssueAndRedirect = leaf("recordIssueAndRedirect");
		public final PathNode saveIssue = leaf("saveIssue");
	}

	public static class ErrorGeneratorPaths extends PathNode{
		public final PathNode generate = leaf("generate");
	}

}
