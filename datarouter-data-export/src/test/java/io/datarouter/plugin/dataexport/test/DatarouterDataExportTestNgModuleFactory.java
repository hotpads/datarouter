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
package io.datarouter.plugin.dataexport.test;

import java.util.List;

import io.datarouter.client.memory.test.DatarouterMemoryTestClientIds;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin.DatarouterNodewatchPluginBuilder;
import io.datarouter.nodewatch.config.DatarouterStorageStatsDirectorySupplier.NoOpStorageStatsDirectorySupplier;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.storage.config.properties.DatarouterTestPropertiesFile;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;

public class DatarouterDataExportTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterDataExportTestNgModuleFactory(){
		super(List.of(
				new DatarouterWebGuiceModule(),
				new DataExportGuiceModule()));
	}

	public static class DataExportGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bindActualInstance(
					ServiceName.class,
					new ServiceName("datarouter-data-export"));
			bindActualInstance(
					DatarouterTestPropertiesFile.class,
					new DatarouterTestPropertiesFile("memory.properties"));
			bindActual(
					DatarouterDataExportDirectorySupplier.class,
					TestDatarouterDataExportDirectorySupplier.class);
			installNodewatch();
		}

		private void installNodewatch(){
			var nodewatchPlugin = new DatarouterNodewatchPluginBuilder(
					List.of(DatarouterMemoryTestClientIds.MEMORY),
					NoOpStorageStatsDirectorySupplier.class)
					.build();
			install(nodewatchPlugin);

			//TODO the Plugin could install the Daos
			install(nodewatchPlugin.getDaosModuleBuilder());
			bindActualInstance(
					DaoClasses.class,
					new DaoClasses(nodewatchPlugin.getDaosModuleBuilder().getDaoClasses()));
		}

	}

}
