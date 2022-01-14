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

import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.plugin.copytable.CopyTableJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry.NoOpTableProcessorRegistry;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorTestableService;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterCopyTablePlugin extends BaseJobletPlugin{

	private static final DatarouterCopyTablePaths PATHS = new DatarouterCopyTablePaths();

	private final Class<? extends TableProcessorRegistry> tableProcessorRegistry;

	private DatarouterCopyTablePlugin(
			Class<? extends TableProcessorRegistry> tableProcessorRegistry){
		this.tableProcessorRegistry = tableProcessorRegistry;

		addRouteSet(DatarouterCopyTableRouteSet.class);
		addJobletType(CopyTableJoblet.JOBLET_TYPE);
		addJobletType(TableProcessorJoblet.JOBLET_TYPE);
		addTestable(TableProcessorTestableService.class);

		addDatarouterNavBarItem(
				DatarouterNavBarCategory.TOOLS,
				PATHS.datarouter.copyTableJoblets,
				"Copy Table - Joblets");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.TOOLS,
				PATHS.datarouter.copyTableSingleThread,
				"Copy Table - Single Thread");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.TOOLS,
				PATHS.datarouter.tableProcessorJoblets,
				"Table Processor - Joblets");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.TOOLS,
				PATHS.datarouter.tableProcessorSingleThread,
				"Table Processor - Single Thread");

		addDatarouterGithubDocLink("datarouter-copy-table");
	}

	@Override
	public void configure(){
		bind(TableProcessorRegistry.class).to(tableProcessorRegistry);
	}

	public static class DatarouterCopyTablePluginBuilder{

		private Class<? extends TableProcessorRegistry> tableProcessorRegistry = NoOpTableProcessorRegistry.class;

		public DatarouterCopyTablePluginBuilder withTableProcessorRegistry(
				Class<? extends TableProcessorRegistry> tableProcessorRegistry){
			this.tableProcessorRegistry = tableProcessorRegistry;
			return this;
		}

		public DatarouterCopyTablePlugin build(){
			return new DatarouterCopyTablePlugin(
					tableProcessorRegistry);
		}

	}

}
