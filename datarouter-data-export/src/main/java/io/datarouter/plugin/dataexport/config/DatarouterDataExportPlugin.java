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

import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterDataExportPlugin extends BaseWebPlugin{

	private static final DatarouterDataExportPaths PATHS = new DatarouterDataExportPaths();

	private final Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass;

	private DatarouterDataExportPlugin(
			Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass){
		this.directorySupplierClass = directorySupplierClass;
		addRouteSet(DatarouterDataExportRouteSet.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.DATA,
				PATHS.datarouter.dataExport.exportDatabeans.singleTable,
				"Table - Exporter");
	}

	@Override
	protected void configure(){
		bind(DatarouterDataExportDirectorySupplier.class).to(directorySupplierClass);
	}

	public static class DatarouterDataExportPluginBuilder{

		private final Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass;

		public DatarouterDataExportPluginBuilder(
				Class<? extends DatarouterDataExportDirectorySupplier> directorySupplierClass){
			this.directorySupplierClass = directorySupplierClass;
		}

		public DatarouterDataExportPlugin build(){
			return new DatarouterDataExportPlugin(
					directorySupplierClass);
		}

	}

}
