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
package io.datarouter.client.hbase.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberTool;

@Singleton
public class DatarouterHBaseSettingRoot extends SettingRoot{

	public final Integer executorThreadCount;
	public final Integer executorQueueSize;
	public final CachedSetting<Boolean> runHbaseRegionBalancerJob;
	public final CachedSetting<Integer> regionBalancerRegionsPerMinute;
	public final CachedSetting<Boolean> runHbaseCompactionJob;
	public final CachedSetting<Boolean> enablePrefetching;

	@Inject
	public DatarouterHBaseSettingRoot(SettingFinder finder){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterHBase.");
		executorThreadCount = registerInteger("executorThreadCount", 100).get();//fixed after startup
		executorQueueSize = registerInteger("executorQueueSize", 1).get();//fixed after startup
		runHbaseRegionBalancerJob = registerBoolean("runHbaseRegionBalancerJob", false);
		regionBalancerRegionsPerMinute = registerInteger("regionBalancerRegionsPerMinute", 250);
		runHbaseCompactionJob = registerBoolean("runHbaseCompactionJob", false);
		enablePrefetching = registerBoolean("enablePrefetching", true);
	}

	public long getSleepBetweenRegionMovementMs(){
		long rpm = regionBalancerRegionsPerMinute.get();
		long msBetweenRegions = DateTool.MILLISECONDS_IN_MINUTE / rpm;
		return msBetweenRegions;
	}

	public boolean shouldRunHBaseRegionBalancerJob(){
		return runHbaseRegionBalancerJob.get() && NumberTool.isPositive(regionBalancerRegionsPerMinute.get());
	}

}
