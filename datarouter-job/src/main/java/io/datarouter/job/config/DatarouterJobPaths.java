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
package io.datarouter.job.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.path.PathsRoot;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;

@Singleton
public class DatarouterJobPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter;

	@Inject
	public DatarouterJobPaths(DatarouterTaskTrackerPaths datarouterTaskTrackerPaths){
		this.datarouter = branch(DatarouterPaths::new, datarouterTaskTrackerPaths.datarouter.getValue());
	}

	public static class DatarouterPaths extends PathNode{
		public final TriggerPaths triggers = branch(TriggerPaths::new, "triggers");
	}

	public static class TriggerPaths extends PathNode{
		public final PathNode list = leaf("list");
	}

}
