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
package io.datarouter.web.plugins.opencencus.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.Metrics;
import jakarta.inject.Singleton;

@Singleton
public class DifferencingCounterService{
	private static final Logger logger = LoggerFactory.getLogger(DifferencingCounterService.class);

	private static final int GRACE_PERIOD_MS = 1_000;

	private final Map<String,DifferencingCounter> previousGauge = new ConcurrentHashMap<>();

	public void add(String key, long value, int expectedFrequencyMs){
		DifferencingCounter newDto = new DifferencingCounter(value, System.currentTimeMillis());
		DifferencingCounter previousDto = previousGauge.put(key, newDto);
		if(previousDto == null){
			return;
		}
		long valueDifference = newDto.value - previousDto.value;
		long dateDifferenceMs = newDto.dateMs - previousDto.dateMs;
		if(dateDifferenceMs > expectedFrequencyMs + GRACE_PERIOD_MS){
			logger.warn("late value, discarding, dateDifferenceMs={} valueDifference={} key={}",
					dateDifferenceMs,
					valueDifference,
					key);
			return;
		}
		logger.info("dateDifferenceMs={} key={}",
				dateDifferenceMs,
				key);
		if(valueDifference >= 0){
			Metrics.count(key, valueDifference);
		}
	}

	private record DifferencingCounter(
			long value,
			long dateMs){
	}

}
