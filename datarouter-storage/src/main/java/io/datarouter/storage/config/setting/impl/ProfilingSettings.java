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
package io.datarouter.storage.config.setting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{

	public final Setting<Boolean> saveCounts;
	public final Setting<Boolean> bufferCountsInSqs;//currently need to restart webapp after changing
	public final Setting<Boolean> drainSqsCounts;
	public final Setting<Boolean> bufferTracesInSqs;
	public final Setting<Boolean> drainSqsTraces;
	public final Setting<Boolean> runMetricsAggregationJob;
	public final Setting<Boolean> runServerMonitoringJob;
	public final Setting<Boolean> runLatencyMonitoringJob;
	public final Setting<Boolean> saveExecutorsMetrics;
	public final Setting<Boolean> saveHttpClientsMetrics;
	public final Setting<Boolean> runAvailabilitySwitchJob;

	@Inject
	public ProfilingSettings(SettingFinder finder){
		super(finder, "datarouter.profiling.");

		saveCounts = registerBoolean("saveCounts", true);
		bufferCountsInSqs = registerBoolean("bufferCountsInSqs", false);
		drainSqsCounts = registerBoolean("drainSqsCounts", false);
		bufferTracesInSqs = registerBoolean("bufferTracesInSqs", false);
		drainSqsTraces = registerBoolean("drainSqsTraces", false);
		runMetricsAggregationJob = registerBoolean("runMetricsAggregationJob", false);
		runServerMonitoringJob = registerBoolean("runServerMonitoringJob", true);
		runLatencyMonitoringJob = registerBoolean("runLatencyMonitoringJob", false);
		saveExecutorsMetrics = registerBoolean("saveExecutorsMetrics", false);
		saveHttpClientsMetrics = registerBoolean("saveHttpClientsMetrics", false);
		runAvailabilitySwitchJob = registerBoolean("runAvailabilitySwitchJob", false);
	}

	public Setting<Boolean> getSaveCounts(){
		return saveCounts;
	}

	public Setting<Boolean> getBufferCountsInSqs(){
		return bufferCountsInSqs;
	}

	public Setting<Boolean> getDrainSqsCounts(){
		return drainSqsCounts;
	}

	public Setting<Boolean> getBufferTracesInSqs(){
		return bufferTracesInSqs;
	}

	public Setting<Boolean> getDrainSqsTraces(){
		return drainSqsTraces;
	}

	public Setting<Boolean> getRunMetricsAggregationJob(){
		return runMetricsAggregationJob;
	}

	public Setting<Boolean> getRunServerMonitoringJob(){
		return runServerMonitoringJob;
	}

}
