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
package io.datarouter.exception.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.exceptionrecord.BaseExceptionRecord;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterExceptionSettingRoot extends SettingRoot{

	//publish ExceptionRecords from buffer
	public final CachedSetting<Boolean> runExceptionRecordMemoryToPublisherConveyor;
	//controls ExceptionRecords publishing destination
	public final CachedSetting<Boolean> saveExceptionRecordBlobsToQueueDaoInsteadOfDirectoryDao;
	public final CachedSetting<Integer> exceptionRecordConveyorThreadCount;

	//publish HttpRequestRecords from buffer
	public final CachedSetting<Boolean> runHttpRequestRecordMemoryToPublisherConveyor;
	//controls ExceptionRecords publishing destination
	public final CachedSetting<Boolean> saveHttpRequestRecordBlobsToQueueDaoInsteadOfDirectoryDao;
	public final CachedSetting<Integer> httpRequestRecordConveyorThreadCount;

	public final CachedSetting<Boolean> forceHideStackTrace;
	public final CachedSetting<String> exceptionRecorderDomainName;
	public final CachedSetting<Boolean> shouldReport;

	public final CachedSetting<Boolean> runExceptionRecordAggregationJob;
	public final CachedSetting<Boolean> runExceptionRecordVacuum;
	public final CachedSetting<Boolean> runHttpRequestRecordVacuumJob;

	public final CachedSetting<Boolean> runExceptionRecordMemoryToDatabaseConveyor;
	public final CachedSetting<Boolean> runHttpRequestRecordMemoryToDatabaseConveyor;

	public final CachedSetting<Boolean> publishRecords;
	public final CachedSetting<Integer> maxPublisherStackTraceLength;

	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;

	public final Setting<Integer> exceptionRecordPublishThreadCount;
	public final Setting<Integer> httpRequestRecordPublishThreadCount;
	public final Setting<Integer> exceptionRecordMemoryToDatabaseThreadCount;
	public final Setting<Integer> httpRequestRecordMemoryToDatabaseThreadCount;

	public final Setting<Integer> exceptionRecordAggregationJobBatchSize;

	@Inject
	public DatarouterExceptionSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterException.");

		runExceptionRecordMemoryToPublisherConveyor = registerBooleans(
				"runExceptionRecordMemoryToPublisherConveyor", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveExceptionRecordBlobsToQueueDaoInsteadOfDirectoryDao = registerBooleans(
				"saveExceptionRecordBlobsToQueueDaoInsteadOfDirectoryDao", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		exceptionRecordConveyorThreadCount = registerInteger("exceptionRecordConveyorThreadCount", 1);

		runHttpRequestRecordMemoryToPublisherConveyor = registerBooleans(
				"runHttpRequestRecordMemoryToPublisherConveyor", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));
		saveHttpRequestRecordBlobsToQueueDaoInsteadOfDirectoryDao = registerBoolean(
				"saveHttpRequestRecordBlobsToQueueDaoInsteadOfDirectoryDao", false);
		httpRequestRecordConveyorThreadCount = registerInteger("httpRequestRecordConveyorThreadCount", 1);

		forceHideStackTrace = registerBoolean("forceHideStackTrace", false);
		exceptionRecorderDomainName = registerString("exceptionRecorderDomainName", "localhost:8443");
		shouldReport = registerBoolean("shouldReport", false);

		runExceptionRecordAggregationJob = registerBoolean("runExceptionRecordAggregationJob", false);
		runExceptionRecordVacuum = registerBoolean("runExceptionRecordVacuum", false);
		runHttpRequestRecordVacuumJob = registerBoolean("runHttpRequestRecordVacuumJob", false);

		runExceptionRecordMemoryToDatabaseConveyor = registerBoolean("runExceptionRecordMemoryToDatabaseConveyor",
				true);
		runHttpRequestRecordMemoryToDatabaseConveyor = registerBoolean("runHttpRequestRecordMemoryToDatabaseConveyor",
				true);

		maxPublisherStackTraceLength = registerInteger("maxPublisherStackTraceLength",
				BaseExceptionRecord.FieldKeys.stackTrace.getSize());

		publishRecords = registerBooleans("publishRecords", defaultTo(false)
				.withTag(DatarouterSettingTagType.TRACE2PIPELINE, () -> true));

		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);
		exceptionRecordPublishThreadCount = registerInteger("exceptionRecordPublishThreadCount", 1);
		httpRequestRecordPublishThreadCount = registerInteger("httpRequestRecordPublishThreadCount", 1);
		exceptionRecordMemoryToDatabaseThreadCount = registerInteger("exceptionRecordMemoryToDatabaseThreadCount", 1);
		httpRequestRecordMemoryToDatabaseThreadCount = registerInteger("httpRequestRecordMemoryToDatabaseThreadCount",
				1);

		exceptionRecordAggregationJobBatchSize = registerInteger("exceptionRecordAggregationJobBatchSize", 100);
	}

}
