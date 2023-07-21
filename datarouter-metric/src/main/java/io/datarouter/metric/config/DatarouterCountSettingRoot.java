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
package io.datarouter.metric.config;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterCountSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> saveCountStatsToMemory;

	@Inject
	public DatarouterCountSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterCount.");

		saveCountStatsToMemory = registerBooleans("saveCountStatsToMemory", defaultTo(false)
				.withTag(DatarouterSettingTagType.COUNT_PIPELINE, () -> true));
	}

}
