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
package io.datarouter.trace.settings;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterTracePublisherSettingRoot extends SettingRoot{

	//save Traces in buffer
	public final CachedSetting<Boolean> saveTracesToMemory;
	//publish Traces from buffer
	public final CachedSetting<Boolean> runTraceMemoryToPublisherConveyor;

	//controls Trace publishing destination
	public final CachedSetting<Boolean> saveTraceBlobsToQueueDaoInsteadOfDirectoryDao;

	public final CachedSetting<Integer> conveyorThreadCount;

	@Inject
	public DatarouterTracePublisherSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTracePublisher.");

		saveTracesToMemory = registerBooleans("saveTracesToMemory", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		runTraceMemoryToPublisherConveyor = registerBooleans(
				"runTraceMemoryToPublisherConveyor",
				defaultTo(false)
						.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveTraceBlobsToQueueDaoInsteadOfDirectoryDao = registerBooleans(
				"saveTraceBlobsToQueueDaoInsteadOfDirectoryDao",
				defaultTo(false)
						.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		conveyorThreadCount = registerInteger("conveyorThreadCount", 1);
	}

}
