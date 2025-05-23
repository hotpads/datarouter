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

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterTraceFilterSettingRoot extends SettingRoot{

	public final CachedSetting<Integer> logTracesOverMs;
	public final CachedSetting<Boolean> saveTraces;
	public final CachedSetting<Integer> saveTracesOverMs;
	public final CachedSetting<Integer> saveTracesCpuOverMs;
	public final Setting<Boolean> addTraceparentHeader;
	public final CachedSetting<String> traceDomain;
	public final Setting<Boolean> recordAllLatency;
	public final CachedSetting<Boolean> savePayloadSizeBytes;
	public final CachedSetting<Boolean> saveTraceCpuTime;
	public final CachedSetting<Boolean> saveTraceAllocatedBytes;
	public final CachedSetting<Boolean> saveThreadCpuTime;
	public final CachedSetting<Boolean> saveThreadMemoryAllocated;
	public final CachedSetting<Boolean> saveSpanCpuTime;
	public final CachedSetting<Boolean> saveSpanMemoryAllocated;
	public final CachedSetting<Integer> randomSamplingMax;
	public final CachedSetting<Integer> randomSamplingThreshold;
	public final CachedSetting<Integer> maxSpansPerTrace;
	public final CachedSetting<Boolean> publishNonProdDataToSharedQueue;

	@Inject
	public DatarouterTraceFilterSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTraceFilter.");

		logTracesOverMs = registerInteger("logTracesOverMs", 500);
		saveTraces = registerBooleans("saveTraces", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2_PIPELINE, () -> true));
		saveTracesOverMs = registerIntegers("saveTracesOverMs", defaultTo(500)
				.withTag(DatarouterSettingTagType.TRACE2_PIPELINE, () -> 500));
		saveTracesCpuOverMs = registerIntegers("saveTracesCpuOverMs", defaultTo(500));
		addTraceparentHeader = registerBoolean("addTraceparentHeader", true);
		traceDomain = registerString("traceDomain", "localhost:8443");
		saveTraceCpuTime = registerBoolean("saveTraceCpuTime", true);
		recordAllLatency = registerBoolean("recordAllLatency", true);
		savePayloadSizeBytes = registerBoolean("savePayloadSizeBytes", true);
		saveTraceAllocatedBytes = registerBoolean("saveTraceAllocatedBytes", true);
		saveThreadCpuTime = registerBooleans("saveThreadCpuTime", defaultTo(true));
		saveThreadMemoryAllocated = registerBooleans("saveThreadMemoryAllocated", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2_PIPELINE, () -> true));
		saveSpanCpuTime = registerBooleans("saveSpanCpuTime", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2_PIPELINE, () -> true));
		saveSpanMemoryAllocated = registerBooleans("saveSpanMemoryAllocated", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2_PIPELINE, () -> true));

		randomSamplingMax = registerInteger("randomSamplingMax", 10000);
		randomSamplingThreshold = registerInteger("randomSamplingThreshold", 5);
		maxSpansPerTrace = registerInteger("maxSpansPerTrace", 1400);
		publishNonProdDataToSharedQueue = registerBoolean("publishNonProdDataToSharedQueue", false);
	}

}
