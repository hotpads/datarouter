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
package io.datarouter.joblet.config;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobletPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final JobletPaths joblets = branch(JobletPaths::new, "joblets");
	}

	public static class JobletPaths extends PathNode{
		public final PathNode copyJobletRequestsToQueues = leaf("copyJobletRequestsToQueues");
		public final PathNode createSleepingJoblets = leaf("createSleepingJoblets");
		public final PathNode deleteGroup = leaf("deleteGroup");
		public final PathNode exceptions = leaf("exceptions");
		public final PathNode kill = leaf("kill");
		public final PathNode list = leaf("list");
		public final PathNode queues = leaf("queues");
		public final PathNode restart = leaf("restart");
		public final PathNode running = leaf("running");
		public final PathNode threadCounts = leaf("threadCounts");
		public final PathNode timeoutStuckRunning = leaf("timeoutStuckRunning");
	}

}
