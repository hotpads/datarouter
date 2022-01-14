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

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.count.CountBatchDto;
import io.datarouter.metric.DatarouterMetricExecutors.DatarouterCountFlushSchedulerExecutor;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.counter.CountBlobService;
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;
import io.datarouter.util.number.RandomTool;

public class CountFlusher{
	private static final Logger logger = LoggerFactory.getLogger(CountFlusher.class);

	private final String serviceName;
	private final String serverName;
	private final Gson gson;
	private final DatarouterCountPublisherDao publisherDao;
	private final CountBlobService countBlobService;
	private final Queue<Map<Long,Map<String,Long>>> flushQueue;
	private final DatarouterCountFlushSchedulerExecutor flushScheduler;
	private final DatarouterCountSettingRoot settings;

	private CountFlusher(String serviceName, String serverName, Gson gson, DatarouterCountPublisherDao publisherDao,
			CountBlobService countBlobService, DatarouterCountFlushSchedulerExecutor flushScheduler,
			DatarouterCountSettingRoot settings){
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.gson = gson;
		this.publisherDao = publisherDao;
		this.countBlobService = countBlobService;
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
			if(!settings.saveCounts.get() && !settings.saveCountBlobs.get()){
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
				var dto = new CountBatchDto(RandomTool.nextPositiveLong(), serviceName, serverName, counts);
				var json = gson.toJson(dto);
				var message = new ConveyorMessage(UlidTool.nextUlid(), json);
				logCountsSpec(counts, json);
				if(settings.saveCounts.get()){
					if(settings.saveCountBlobs.get() && settings.skipsSaveCountsWhenSaveCountBlobsIsTrue.get()){
						logger.warn("skipping save. saving only to blobs instead");
					}else{
						publisherDao.put(message);
					}
				}
				if(settings.saveCountBlobs.get()){
					flushCountBlobs(counts);
				}
				flushQueue.poll();
			}
		}catch(Throwable e){
			logger.warn("", e);
		}
	}

	private void flushCountBlobs(Map<Long,Map<String,Long>> countBlobs){
		if(settings.saveCountBlobs.get()){
			//TODO try/catch is a temporary safeguard. shouldn't be necessary
			try{
				countBlobService.add(new CountBatchDto(null, serviceName, serverName, countBlobs));
			}catch(RuntimeException e){
				logger.error("ignoring count blob publishing failure", e);
			}
		}
	}

	private static void logCountsSpec(Map<Long,Map<String,Long>> counts, String countsJson){
		int names = Scanner.of(counts.values())
				.map(Map::size)
				.reduce(0, Integer::sum);
		logger.info("counts buckets={}, names={}, jsonLength={}", counts.size(), names, countsJson.length());
	}

	@Singleton
	public static class CountFlusherFactory{

		@Inject
		private Gson gson;
		@Inject
		private CountBlobService countBlobService;
		@Inject
		private DatarouterCountPublisherDao publisherDao;
		@Inject
		private DatarouterCountFlushSchedulerExecutor flushScheduler;
		@Inject
		private DatarouterCountSettingRoot settings;

		public CountFlusher create(String serviceName, String serverName){
			return new CountFlusher(
					serviceName,
					serverName,
					gson,
					publisherDao,
					countBlobService,
					flushScheduler,
					settings);
		}

	}

}
