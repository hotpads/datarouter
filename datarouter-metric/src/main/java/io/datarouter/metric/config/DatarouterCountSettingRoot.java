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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterCountSettingRoot extends SettingRoot{

	//save counts in buffer
	public final CachedSetting<Boolean> saveCountsToMemory;
	//publish counts from buffer
	public final CachedSetting<Boolean> runCountMemoryToPublisherConveyor;
	//controls gauge publishing destination
	public final CachedSetting<Boolean> saveCountsToQueueDaoInsteadOfDirectoryDao;

	public final CachedSetting<Integer> conveyorThreadCount;

	@Inject
	public DatarouterCountSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterCount.");

		saveCountsToMemory = registerBooleans("saveCountsToMemory", defaultTo(false)
				.withTag(DatarouterSettingTagType.COUNTPIPELINE, () -> true));
		runCountMemoryToPublisherConveyor = registerBooleans("runCountMemoryToPublisherConveyor", defaultTo(false)
				.withTag(DatarouterSettingTagType.COUNTPIPELINE, () -> true));

		saveCountsToQueueDaoInsteadOfDirectoryDao = registerBoolean(
				"saveCountsToQueueDaoInsteadOfDirectoryDao", false);

		conveyorThreadCount = registerInteger("conveyorThreadCount", 1);
	}

}
