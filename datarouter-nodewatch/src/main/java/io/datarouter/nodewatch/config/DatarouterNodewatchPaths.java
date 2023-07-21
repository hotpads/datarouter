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
package io.datarouter.nodewatch.config;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNodewatchPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final NodewatchPaths nodewatch = branch(NodewatchPaths::new, "nodewatch");
	}

	public static class NodewatchPaths extends PathNode{
		public final PathNode tables = leaf("tables");
		public final PathNode summary = leaf("summary");
		public final PathNode configs = leaf("configs");
		public final PathNode slowSpans = leaf("slowSpans");
		public final MetadataPaths metadata = branch(MetadataPaths::new, "metadata");
		public final ThresholdPaths threshold = branch(ThresholdPaths::new, "threshold");
		public final TablePaths table = branch(TablePaths::new, "table");
	}

	public static class MetadataPaths extends PathNode{
		public final PathNode migrate = leaf("migrate");
	}

	public static class TablePaths extends PathNode{
		public final PathNode resample = leaf("resample");
		public final PathNode deleteSamples = leaf("deleteSamples");
		public final PathNode deleteAllMetadata = leaf("deleteAllMetadata");
		public final PathNode nodeName = leaf("nodeName");
		public final PathNode storage = leaf("storage");
	}

	public static class ThresholdPaths extends PathNode{
		public final PathNode edit = leaf("edit");
		public final PathNode delete = leaf("delete");
	}

}
