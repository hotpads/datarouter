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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class CountMemoryToPublisherConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(CountMemoryToPublisherConveyor.class);

	private final MemoryBuffer<Map<Long,Map<String,Long>>> buffer;
	private final CountPublisher countPublisher;

	public CountMemoryToPublisherConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<Map<Long,Map<String,Long>>> buffer,
			ExceptionRecorder exceptionRecorder,
			CountPublisher countPublisher){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.buffer = buffer;
		this.countPublisher = countPublisher;
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<Map<Long,Map<String,Long>>> dtos = buffer.pollMultiWithLimit(1);//TODO probably 1
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			Map<Long,Map<String,Long>> counts = dtos.get(0);///TODO assuming size 1
			//TODO old checking logic. remove after fixing concurrency issues.
			Scanner.of(counts.keySet())//TODO
					.include(timestamp -> counts.get(timestamp).isEmpty())//TODO
					.map(timestamp -> timestamp.toString())
					.flush(emptyTimestamps -> {
						if(emptyTimestamps.size() > 0){
							logger.warn("found empty maps for timestamps={}", String.join(",", emptyTimestamps));
						}
					});

			logCountsSpec(counts);
			countPublisher.add(counts);
			//TODO get actual size somehow?
			//--from Map/DTO?
			//--from publisher response?
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
		}
		return new ProcessBatchResult(false);
	}

	//TODO old logic. remove after fixing concurrency issues.
	private static void logCountsSpec(Map<Long,Map<String,Long>> counts){
		int names = Scanner.of(counts.values())
				.map(Map::size)
				.reduce(0, Integer::sum);
		logger.info("counts buckets={}, names={}", counts.size(), names);
	}

}