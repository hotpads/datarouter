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
package io.datarouter.nodewatch.shadowtable.config;

import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.nodewatch.shadowtable.ShadowTableConfig;
import io.datarouter.nodewatch.shadowtable.ShadowTableConfig.GenericShadowTableConfig;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterShadowTablePlugin extends BaseWebPlugin{

	public static final String NAME = "ShadowTable";

	private final Class<? extends DatarouterShadowTableDirectorySupplier> shadowTableDirectorySupplierClass;
	private final List<ShadowTableExport> shadowTableExports;

	private DatarouterShadowTablePlugin(
			Class<? extends DatarouterShadowTableDirectorySupplier> shadowTableDirectorySupplierClass,
			List<ShadowTableExport> shadowTableExports){
		this.shadowTableDirectorySupplierClass = shadowTableDirectorySupplierClass;
		this.shadowTableExports = shadowTableExports;

		addSettingRoot(DatarouterShadowTableSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterShadowTableTriggerGroup.class);
	}

	@Override
	public void configure(){
		bind(DatarouterShadowTableDirectorySupplier.class).to(shadowTableDirectorySupplierClass);
		bindActualInstance(ShadowTableConfig.class, new GenericShadowTableConfig(shadowTableExports));
	}

	public static class DatarouterShadowTablePluginBuilder{

		private final Class<? extends DatarouterShadowTableDirectorySupplier> shadowTableDirectorySupplierClass;
		private final List<ShadowTableExport> shadowTableExports;

		public DatarouterShadowTablePluginBuilder(
				Class<? extends DatarouterShadowTableDirectorySupplier> shadowTableDirectorySupplierClass,
				List<ShadowTableExport> shadowTableExports){
			this.shadowTableDirectorySupplierClass = shadowTableDirectorySupplierClass;
			this.shadowTableExports = shadowTableExports;
		}

		public DatarouterShadowTablePlugin build(){
			return new DatarouterShadowTablePlugin(
					shadowTableDirectorySupplierClass,
					shadowTableExports);
		}

	}

}
