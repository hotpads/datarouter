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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterTraceLocalSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> bufferInSqs;
	public final CachedSetting<Boolean> bufferInSqsForTrace2;
	public final CachedSetting<Boolean> runMemoryToSqs;
	public final CachedSetting<Boolean> runMemoryToSqsForTrace2;
	public final CachedSetting<Boolean> drainSqsToLocal;
	public final CachedSetting<Boolean> drainSqsToLocalForTrace2;
	public final CachedSetting<Boolean> drainSqsToLocalForTrace2HttpRequestRecord;

	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;

	// Jobs are not registered by default
	public final CachedSetting<Boolean> runVacuumJob;

	@Inject
	public DatarouterTraceLocalSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTraceLocal.");
		bufferInSqs = registerBoolean("bufferInSqs", true);
		bufferInSqsForTrace2 = registerBooleans("bufferInSqsForTrace2", defaultTo(false));
		runMemoryToSqs = registerBoolean("runMemoryToSqs", false);
		runMemoryToSqsForTrace2 = registerBooleans("runMemoryToSqsForTrace2", defaultTo(false));
		drainSqsToLocal = registerBoolean("drainSqsToLocal", false);
		drainSqsToLocalForTrace2 = registerBooleans("drainSqsToLocalForTrace2", defaultTo(false));
		drainSqsToLocalForTrace2HttpRequestRecord = registerBooleans("drainSqsToLocalForTrace2HttpRequestRecord",
				defaultTo(false));
		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);

		runVacuumJob = registerBoolean("runVacuumJob", true);
	}

}
