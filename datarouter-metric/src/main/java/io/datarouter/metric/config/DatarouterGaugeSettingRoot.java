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
public class DatarouterGaugeSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> saveGauges;

	/**
	 * Additional setting to control running gauges from memory to queue. Could be useful if you want to drain the
	 * buffer and not send messages to queues.
	 */
	public final CachedSetting<Boolean> sendGaugesFromMemoryToQueue;

	/**
	 * Used to run gauges from memory to queues
	 */
	public final CachedSetting<Boolean> runGaugeMemoryToQueue;

	/**
	 * Used to run gauges from queues to publisher
	 */
	public final CachedSetting<Boolean> runGaugeQueueToPublisher;

	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;
	public final CachedSetting<Integer> memoryConveyorThreadCount;
	public final CachedSetting<Integer> drainConveyorThreadCount;

	@Inject
	public DatarouterGaugeSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterGauge.");

		saveGauges = registerBooleans("saveGauges", defaultTo(false)
				.withTag(DatarouterSettingTagType.GAUGEPIPELINE, () -> true));
		sendGaugesFromMemoryToQueue = registerBooleans("sendGaugesFromMemoryToQueue", defaultTo(false)
				.withTag(DatarouterSettingTagType.GAUGEPIPELINE, () -> true));

		runGaugeMemoryToQueue = registerBooleans("runGaugeMemoryToQueue", defaultTo(false)
				.withTag(DatarouterSettingTagType.GAUGEPIPELINE, () -> true));
		runGaugeQueueToPublisher = registerBooleans("runGaugeQueueToPublisher", defaultTo(false)
				.withTag(DatarouterSettingTagType.GAUGEPIPELINE, () -> true));

		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);
		memoryConveyorThreadCount = registerInteger("memoryConveyorThreadCount", 2);
		drainConveyorThreadCount = registerInteger("drainConveyorThreadCount", 2);
	}

}
