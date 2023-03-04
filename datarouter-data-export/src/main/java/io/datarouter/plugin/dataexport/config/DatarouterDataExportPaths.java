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
package io.datarouter.plugin.dataexport.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterDataExportPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final DataMigrationPaths dataMigration = branch(DataMigrationPaths::new, "dataMigration");
	}

	public static class DataMigrationPaths extends PathNode{
		public final PathNode showForm = leaf("showForm");
		public final PathNode exportData = leaf("exportData");
		public final PathNode importFromS3 = leaf("importFromS3");
		public final PathNode importS3KeyToNode = leaf("importS3KeyToNode");
		public final PathNode localImport = leaf("localImport");
	}

}
