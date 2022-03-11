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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.metric.DatarouterMetricExecutors.DatarouterCountFlushSchedulerExecutor;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.scanner.Scanner;

@Singleton
public class CountFlusher{
	private static final Logger logger = LoggerFactory.getLogger(CountFlusher.class);

	private final CountPublisher countPublisher;
	private final Queue<Map<Long,Map<String,Long>>> flushQueue;
	private final DatarouterCountFlushSchedulerExecutor flushScheduler;
	private final DatarouterCountSettingRoot settings;

	@Inject
	public CountFlusher(CountPublisher countPublisher, DatarouterCountFlushSchedulerExecutor flushScheduler,
			DatarouterCountSettingRoot settings){
		this.countPublisher = countPublisher;
		this.flushQueue = new ArrayBlockingQueue<>(60);//careful, size() must iterate every element
		this.flushScheduler = flushScheduler;
		this.settings = settings;

		this.flushScheduler.scheduleWithFixedDelay(this::flush, 0, 1, TimeUnit.SECONDS);
	}

	public void saveCounts(Map<Long,Map<String,Long>> counts){
		if(!flushQueue.offer(counts)){
			logger.warn("flushQueue rejected our counts");
		}
	}

	private void flush(){
		try{
			if(!settings.saveCountBlobs.get()){
				flushQueue.clear();
				return;
			}
			while(true){
				Map<Long,Map<String,Long>> counts = flushQueue.peek();
				if(counts == null){
					return;
				}
				Scanner.of(counts.keySet())
						.include(timestamp -> counts.get(timestamp).isEmpty())
						.map(timestamp -> timestamp.toString())
						.flush(emptyTimestamps -> {
							if(emptyTimestamps.size() > 0){
								logger.warn("found empty maps for timestamps={}", String.join(",", emptyTimestamps));
							}
						});
				logCountsSpec(counts);
				countPublisher.add(counts);
				flushQueue.poll();
			}
		}catch(Throwable e){
			logger.warn("", e);
		}
	}

	private static void logCountsSpec(Map<Long,Map<String,Long>> counts){
		int names = Scanner.of(counts.values())
				.map(Map::size)
				.reduce(0, Integer::sum);
		logger.info("counts buckets={}, names={}", counts.size(), names);
	}

}
