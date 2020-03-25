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
package io.datarouter.metric.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterGaugeSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> saveGauges;

	/**
	 * Additional setting to control running gauges from memory to sqs. Could be useful if you want to drain the buffer
	 * and not send messages to sqs.
	 */
	public final CachedSetting<Boolean> sendGaugesFromMemoryToSqs;

	/**
	 * Used to run gauges from memory to sqs
	 */
	public final CachedSetting<Boolean> runGaugeMemoryToSqs;

	/**
	 * Used to run gauges from sqs to publisher
	 */
	public final CachedSetting<Boolean> runGaugeSqsToPublisher;

	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;
	public final CachedSetting<Integer> memoryConveyorThreadCount;
	public final CachedSetting<Integer> drainConveyorThreadCount;

	@Inject
	public DatarouterGaugeSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterGauge.");

		saveGauges = registerBoolean("saveGauges", false);
		sendGaugesFromMemoryToSqs = registerBoolean("sendGaugesFromMemoryToSqs", false);

		runGaugeMemoryToSqs = registerBoolean("runGaugeMemoryToSqs", false);
		runGaugeSqsToPublisher = registerBoolean("runGaugeSqsToPublisher", false);

		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);
		memoryConveyorThreadCount = registerInteger("memoryConveyorThreadCount", 2);
		drainConveyorThreadCount = registerInteger("drainConveyorThreadCount", 2);
	}

}
