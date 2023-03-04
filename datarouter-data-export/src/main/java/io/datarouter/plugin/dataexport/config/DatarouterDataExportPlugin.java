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

import java.util.List;

import io.datarouter.plugin.dataexport.storage.DataExportDao;
import io.datarouter.plugin.dataexport.storage.DataExportDao.DatarouterDataExportDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterDataExportPlugin extends BaseWebPlugin{

	public static final String NODE_WIDGET_DATA_EXPORT_PATH = new DatarouterDataExportPaths().datarouter.dataMigration
			.showForm.toSlashedString();

	private final Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass;

	private DatarouterDataExportPlugin(
			DatarouterDataExportDaoModule daosModuleBuilder,
			Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass){
		this.directorySupplierClass = directorySupplierClass;
		addRouteSet(DatarouterDataExportRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, NODE_WIDGET_DATA_EXPORT_PATH, "Data Export");
	}

	@Override
	protected void configure(){
		bind(DatarouterDataExportDirectorySupplier.class).to(directorySupplierClass);
	}

	public static class DatarouterDataExportPluginBuilder{

		private final List<ClientId> clientIds;
		private final Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass;

		public DatarouterDataExportPluginBuilder(
				List<ClientId> clientIds,
				Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass){
			this.clientIds = clientIds;
			this.directorySupplierClass = directorySupplierClass;
		}

		public DatarouterDataExportPlugin build(){
			return new DatarouterDataExportPlugin(
					new DatarouterDataExportDaoModule(clientIds),
					directorySupplierClass);
		}

	}

	public static class DatarouterDataExportDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterDataExportClientIds;

		public DatarouterDataExportDaoModule(
				List<ClientId> datarouterDataExportClientIds){
			this.datarouterDataExportClientIds = datarouterDataExportClientIds;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DataExportDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterDataExportDaoParams.class)
					.toInstance(new DatarouterDataExportDaoParams(datarouterDataExportClientIds));
		}

	}

}
