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
package io.datarouter.plugin.copytable.config;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterCopyTablePaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final CopyTablePaths copyTable = branch(CopyTablePaths::new, "copyTable");
		public final SystemTableCopyPaths systemTableCopier = branch(SystemTableCopyPaths::new, "systemTableCopier");
		public final TableProcessorPaths tableProcessor = branch(TableProcessorPaths::new, "tableProcessor");
	}

	public static class CopyTablePaths extends PathNode{
		public final PathNode joblets = leaf("joblets");
		public final PathNode singleThread = leaf("singleThread");
		public final PathNode systemTables = leaf("systemTables");
	}

	public static class TableProcessorPaths extends PathNode{
		public final PathNode joblets = leaf("joblets");
		public final PathNode singleThread = leaf("singleThread");
	}

	public static class SystemTableCopyPaths extends PathNode{
		public final PathNode viewTables = leaf("viewTables");
		public final PathNode listSystemTables = leaf("listSystemTables");
		public final PathNode migrateSystemTables = leaf("migrateSystemTables");
	}

}