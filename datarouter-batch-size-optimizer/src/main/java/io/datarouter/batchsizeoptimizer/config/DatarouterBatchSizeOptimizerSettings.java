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
package io.datarouter.batchsizeoptimizer.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;

@Singleton
public class DatarouterBatchSizeOptimizerSettings extends SettingRoot{

	public final CachedSetting<Boolean> runBatchSizeOptimizingJob;
	public final CachedSetting<Boolean> runOpPerformanceRecordAggregationJob;

	@Inject
	public DatarouterBatchSizeOptimizerSettings(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterBatchSizeOptimizer.");
		runBatchSizeOptimizingJob = registerBoolean("runBatchSizeOptimizingJob", false);
		runOpPerformanceRecordAggregationJob = registerBoolean("runOpPerformanceRecordAggregationJob", false);
	}

}
