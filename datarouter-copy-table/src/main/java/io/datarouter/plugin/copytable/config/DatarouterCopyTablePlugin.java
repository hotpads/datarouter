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
package io.datarouter.plugin.copytable.config;

import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.plugin.copytable.CopyTableConfiguration;
import io.datarouter.plugin.copytable.CopyTableConfiguration.NoOpCopyTableConfiguration;
import io.datarouter.plugin.copytable.CopyTableJoblet;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterCopyTablePlugin extends BaseJobletPlugin{

	private static final DatarouterCopyTablePaths PATHS = new DatarouterCopyTablePaths();

	private final Class<? extends CopyTableConfiguration> copyTableConfiguration;

	private DatarouterCopyTablePlugin(
			Class<? extends CopyTableConfiguration> copyTableConfiguration){
		this.copyTableConfiguration = copyTableConfiguration;

		addRouteSet(DatarouterCopyTableRouteSet.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.copyTableJoblets,
				"Copy Table - Joblets");
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.copyTableSingleThread,
				"Copy Table - Single Thread");
		addJobletType(CopyTableJoblet.JOBLET_TYPE);
	}

	@Override
	public String getName(){
		return "DatarouterCopyTable";
	}

	@Override
	public void configure(){
		bind(CopyTableConfiguration.class).to(copyTableConfiguration);
	}

	public static class DatarouterCopyTablePluginBuilder{

		private Class<? extends CopyTableConfiguration> copyTableConfiguration = NoOpCopyTableConfiguration.class;

		public DatarouterCopyTablePluginBuilder withCopyTableConfiguration(
				Class<? extends CopyTableConfiguration> copyTableConfiguration){
			this.copyTableConfiguration = copyTableConfiguration;
			return this;
		}

		public DatarouterCopyTablePlugin build(){
			return new DatarouterCopyTablePlugin(
					copyTableConfiguration);
		}

	}

}
