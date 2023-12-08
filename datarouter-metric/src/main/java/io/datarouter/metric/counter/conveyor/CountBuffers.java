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
package io.datarouter.metric.counter.conveyor;

import java.util.Map;

import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.util.buffer.MemoryBuffer;
import jakarta.inject.Singleton;

@Singleton
public class CountBuffers{

	public final MemoryBuffer<Map<Long,Map<String,MetricCollectorStats>>> countStatsBuffer;

	public CountBuffers(){
		this.countStatsBuffer = new MemoryBuffer<>("countStatsBuffer", 100);
	}

	public void offerCountStats(Map<Long,Map<String,MetricCollectorStats>> metricDto){
		countStatsBuffer.offer(metricDto);
	}

}