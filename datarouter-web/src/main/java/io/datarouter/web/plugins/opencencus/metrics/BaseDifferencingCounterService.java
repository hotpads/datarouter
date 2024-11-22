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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.Metrics;


public abstract class BaseDifferencingCounterService{
	private static final Logger logger = LoggerFactory.getLogger(BaseDifferencingCounterService.class);

	private static final int GRACE_PERIOD_MS = 1_000;

	public void add(String key, long value, int expectedFrequencyMs){
		add(key, value, expectedFrequencyMs, Metrics::count);
	}

	public void add(String key, long value, int expectedFrequencyMs, BiConsumer<String,Long> counter){
		DifferencingCounter newDto = new DifferencingCounter(value, System.currentTimeMillis());
		Optional<DifferencingCounter> previousDto = getPreviousAndSaveNew(key, newDto);
		if(previousDto.isEmpty()){
			return;
		}
		long valueDifference = newDto.value - previousDto.get().value;
		long dateDifferenceMs = newDto.dateMs - previousDto.get().dateMs;
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
			counter.accept(key, valueDifference);
		}
	}

	protected abstract Optional<DifferencingCounter> getPreviousAndSaveNew(String key, DifferencingCounter newDto);

	public record DifferencingCounter(
			long value,
			long dateMs){
	}

}
