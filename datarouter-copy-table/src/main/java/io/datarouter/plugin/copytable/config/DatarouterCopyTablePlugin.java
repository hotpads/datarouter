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

import io.datarouter.plugin.copytable.CopyTableJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorTestableService;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterCopyTablePlugin extends BaseWebPlugin{

	private static final DatarouterCopyTablePaths PATHS = new DatarouterCopyTablePaths();

	public DatarouterCopyTablePlugin(){

		addRouteSet(DatarouterCopyTableRouteSet.class);
		addPluginEntry(CopyTableJoblet.JOBLET_TYPE);
		addPluginEntry(TableProcessorJoblet.JOBLET_TYPE);
		addTestable(TableProcessorTestableService.class);

		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				PATHS.datarouter.copyTable.joblets,
				"Table - Copier");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				PATHS.datarouter.tableProcessor.joblets,
				"Table - Processor");

		addDatarouterGithubDocLink("datarouter-copy-table");
	}

}
