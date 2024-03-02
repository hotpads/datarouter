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

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterExceptionSettingRoot extends SettingRoot{

	//controls ExceptionRecords publishing destination
	public final CachedSetting<Boolean> saveTaskExecutorRecordsToQueueDaoInsteadOfDirectoryDao;


	public final CachedSetting<Boolean> forceHideStackTrace;
	public final CachedSetting<String> exceptionRecorderDomainName;

	public final CachedSetting<Boolean> runExceptionRecordAggregationJob;
	public final CachedSetting<Boolean> runExceptionRecordVacuum;
	public final CachedSetting<Boolean> runHttpRequestRecordVacuumJob;

	public final CachedSetting<Boolean> saveRecordsLocally;
	public final CachedSetting<Boolean> publishRecords;

	public final CachedSetting<Boolean> compactExceptionLoggingForConveyors;

	public final Setting<Integer> exceptionRecordPublishThreadCount;
	public final Setting<Integer> httpRequestRecordPublishThreadCount;

	public final Setting<Integer> exceptionRecordAggregationJobBatchSize;

	@Inject
	public DatarouterExceptionSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterException.");

		saveTaskExecutorRecordsToQueueDaoInsteadOfDirectoryDao = registerBoolean(
				"saveTaskExecutorRecordsToQueueDaoInsteadOfDirectoryDao", false);

		forceHideStackTrace = registerBoolean("forceHideStackTrace", false);
		exceptionRecorderDomainName = registerString("exceptionRecorderDomainName", "localhost:8443");

		runExceptionRecordAggregationJob = registerBoolean("runExceptionRecordAggregationJob", false);
		runExceptionRecordVacuum = registerBoolean("runExceptionRecordVacuum", false);
		runHttpRequestRecordVacuumJob = registerBoolean("runHttpRequestRecordVacuumJob", false);

		saveRecordsLocally = registerBooleans("saveRecordsLocally", defaultTo(true)
				.withEnvironmentType(EnvironmentType.PRODUCTION, false));
		publishRecords = registerBooleans("publishRecords", defaultTo(false)
				.withTag(DatarouterSettingTagType.EXCEPTION_PIPELINE, () -> true));

		compactExceptionLoggingForConveyors = registerBoolean("compactExceptionLoggingForConveyors", true);
		exceptionRecordPublishThreadCount = registerInteger("exceptionRecordPublishThreadCount", 1);
		httpRequestRecordPublishThreadCount = registerInteger("httpRequestRecordPublishThreadCount", 1);

		exceptionRecordAggregationJobBatchSize = registerInteger("exceptionRecordAggregationJobBatchSize", 100);
	}

}
