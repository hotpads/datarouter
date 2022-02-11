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
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterTracePublisherSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> bufferInSqsForTrace2;
	public final CachedSetting<Boolean> runMemoryToSqsForTrace2;
	public final CachedSetting<Boolean> drainSqsToPublisherForTrace2;
	public final CachedSetting<Boolean> drainSqsToPublisherForTrace2HttpRequestRecord;
	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;
	public final Setting<Integer> runMemoryToSqsForTrace2ThreadCount;
	public final Setting<Integer> drainSqsToPublisherForTrace2ThreadCount;
	public final Setting<Integer> drainSqsToPublisherForTrace2HttpRequestRecordThreadCount;

	@Inject
	public DatarouterTracePublisherSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterTracePublisher.");

		bufferInSqsForTrace2 = registerBooleans("bufferInSqsForTrace2", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		runMemoryToSqsForTrace2 = registerBooleans("runMemoryToSqsForTrace2", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		drainSqsToPublisherForTrace2 = registerBooleans("drainSqsToPublisherForTrace2", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		drainSqsToPublisherForTrace2HttpRequestRecord = registerBooleans(
				"drainSqsToPublisherForTrace2HttpRequestRecord", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);
		runMemoryToSqsForTrace2ThreadCount = registerInteger("runMemoryToSqsForTrace2ThreadCount", 1);
		drainSqsToPublisherForTrace2ThreadCount = registerInteger("drainSqsToPublisherForTrace2ThreadCount", 2);
		drainSqsToPublisherForTrace2HttpRequestRecordThreadCount = registerInteger(
				"drainSqsToPublisherForTrace2HttpRequestRecordThreadCount", 2);
	}

}
