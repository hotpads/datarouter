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
package io.datarouter.conveyor.config;

import java.util.concurrent.TimeUnit;

import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.setting.cached.impl.DurationCachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterConveyorTraceSettings extends SettingNode{

	public final CachedSetting<Boolean> saveTraces;
	public final CachedSetting<Boolean> saveTraceCpuTime;
	public final DurationCachedSetting saveTracesOverMs;
	public final DurationCachedSetting saveTracesCpuOverMs;
	public final CachedSetting<Integer> maxSpansPerTrace;

	@Inject
	public DatarouterConveyorTraceSettings(SettingFinder finder){
		super(finder, "datarouterConveyor.trace.");

		saveTraces = registerBooleans("saveTraces", defaultTo(false)
				.withTag(DatarouterSettingTagType.CONVEYOR_TRACE_PIPELINE, () -> true));
		saveTraceCpuTime = registerBoolean("saveTraceCpuTime", true);
		saveTracesOverMs = registerDurations("saveTracesOverMs",
				defaultTo(new DatarouterDuration(200, TimeUnit.MILLISECONDS))
						.withTag(DatarouterSettingTagType.CONVEYOR_TRACE_PIPELINE,
								() -> new DatarouterDuration(50, TimeUnit.MILLISECONDS)));
		saveTracesCpuOverMs = registerDuration("saveTracesCpuOverMs",
				new DatarouterDuration(50, TimeUnit.MILLISECONDS));
		maxSpansPerTrace = registerInteger("maxSpansPerTrace", 200);
	}

}
