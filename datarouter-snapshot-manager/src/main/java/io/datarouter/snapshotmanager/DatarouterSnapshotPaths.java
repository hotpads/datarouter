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
package io.datarouter.snapshotmanager;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSnapshotPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final SnapshotPaths snapshot = branch(SnapshotPaths::new, "snapshot");
	}

	public static class SnapshotPaths extends PathNode{
		public final PathNode benchmark = leaf("benchmark");
		public final GroupPaths group = branch(GroupPaths::new, "group");
		public final IndividualPaths individual = branch(IndividualPaths::new, "individual");
	}

	public static class GroupPaths extends PathNode{
		public final PathNode listGroups = leaf("listGroups");
		public final PathNode listSnapshots = leaf("listSnapshots");
	}

	public static class IndividualPaths extends PathNode{
		public final PathNode summary = leaf("summary");
		public final PathNode entries = leaf("entries");
		public final PathNode entry = leaf("entry");
	}

}
