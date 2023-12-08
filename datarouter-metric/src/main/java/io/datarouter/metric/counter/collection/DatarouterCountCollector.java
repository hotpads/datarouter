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
package io.datarouter.metric.counter.collection;

import java.util.Map;

import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.counter.conveyor.CountBuffers;
import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.metric.service.AggregatingMetricCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterCountCollector extends AggregatingMetricCollector{

	@Inject
	private CountBuffers countBuffers;
	@Inject
	private DatarouterCountSettingRoot settings;

	@Override
	public boolean saveZeros(){
		return false;
	}

	@Override
	public void offerMetricStats(Map<Long,Map<String,MetricCollectorStats>> metricStats){
		countBuffers.offerCountStats(metricStats);
	}

	@Override
	public boolean saveToMemory(){
		return settings.saveCountStatsToMemory.get();
	}

	public record CountCollectorStats(
			long sum,
			long count,
			long min,
			long max){

		public static CountCollectorStats updateStats(CountCollectorStats prevStats, long delta){
			return new CountCollectorStats(
					prevStats.sum + delta,
					prevStats.count + 1,
					Math.min(prevStats.min, delta),
					Math.max(prevStats.max, delta));
		}

	}

}
