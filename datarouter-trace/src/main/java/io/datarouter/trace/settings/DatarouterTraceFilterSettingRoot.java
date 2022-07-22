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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterTraceFilterSettingRoot extends SettingRoot{

	public final Setting<Boolean> logRequests;
	public final CachedSetting<Integer> logTracesOverMs;
	public final CachedSetting<Boolean> saveTraces;
	public final CachedSetting<Integer> saveTracesOverMs;
	public final CachedSetting<Integer> saveTracesCpuOverMs;
	public final Setting<Boolean> addTraceparentHeader;
	public final CachedSetting<String> traceDomain;
	public final Setting<Set<String>> latencyRecordedHandlers;
	public final Setting<Boolean> recordAllLatency;
	public final CachedSetting<Boolean> saveTraceCpuTime;
	public final CachedSetting<Boolean> saveTraceAllocatedBytes;
	public final CachedSetting<Boolean> saveThreadCpuTime;
	public final CachedSetting<Boolean> saveThreadMemoryAllocated;
	public final CachedSetting<Boolean> saveSpanCpuTime;
	public final CachedSetting<Boolean> saveSpanMemoryAllocated;

	@Inject
	public DatarouterTraceFilterSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTraceFilter.");

		logRequests = registerBoolean("logRequests", false);
		logTracesOverMs = registerInteger("logTracesOverMs", 500);
		saveTraces = registerBooleans("saveTraces", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveTracesOverMs = registerIntegers("saveTracesOverMs", defaultTo(500)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> 500));
		saveTracesCpuOverMs = registerIntegers("saveTracesCpuOverMs", defaultTo(500));
		addTraceparentHeader = registerBoolean("addTraceparentHeader", true);
		traceDomain = registerString("traceDomain", "localhost:8443");
		latencyRecordedHandlers = registerCommaSeparatedStrings("latencyRecordedHandlers", defaultTo(new HashSet<>()));
		saveTraceCpuTime = registerBoolean("saveTraceCpuTime", true);
		recordAllLatency = registerBoolean("recordAllLatency", true);
		saveTraceAllocatedBytes = registerBoolean("saveTraceAllocatedBytes", true);
		saveThreadCpuTime = registerBooleans("saveThreadCpuTime", defaultTo(true));
		saveThreadMemoryAllocated = registerBooleans("saveThreadMemoryAllocated", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveSpanCpuTime = registerBooleans("saveSpanCpuTime", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveSpanMemoryAllocated = registerBooleans("saveSpanMemoryAllocated", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
	}

}
