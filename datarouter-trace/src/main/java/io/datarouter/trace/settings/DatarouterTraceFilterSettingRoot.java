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
package io.datarouter.trace.settings;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
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
	public final Setting<Boolean> addTraceIdHeader;
	public final CachedSetting<String> traceDomain;
	public final Setting<Set<String>> latencyRecordedHandlers;
	public final CachedSetting<Boolean> logCpuTime;

	@Inject
	public DatarouterTraceFilterSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTraceFilter.");

		logRequests = registerBoolean("logRequests", false);
		logTracesOverMs = registerInteger("logTracesOverMs", 500);
		saveTraces = registerBoolean("saveTraces", false);
		saveTracesOverMs = registerInteger("saveTracesOverMs", 500);
		addTraceIdHeader = registerBoolean("addTraceIdHeader", true);
		traceDomain = registerString("traceDomain", "localhost:8443");
		latencyRecordedHandlers = registerCommaSeparatedStrings("latencyRecordedHandlers", defaultTo(new HashSet<>()));
		logCpuTime = registerBoolean("logCpuTime", false);
	}

}
