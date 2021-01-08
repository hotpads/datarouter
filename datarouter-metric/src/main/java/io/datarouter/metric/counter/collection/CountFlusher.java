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
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.UlidTool;

public class CountFlusher{
	private static final Logger logger = LoggerFactory.getLogger(CountFlusher.class);

	private final String serviceName;
	private final String serverName;
	private final Gson gson;
	private final DatarouterCountPublisherDao publisherDao;
	private final Queue<Map<Long,Map<String,Long>>> flushQueue;
	private final DatarouterCountFlushSchedulerExecutor flushScheduler;
	private final Setting<Boolean> saveCounts;

	private CountFlusher(String serviceName, String serverName, Gson gson, DatarouterCountPublisherDao publisherDao,
			DatarouterCountFlushSchedulerExecutor flushScheduler, Setting<Boolean> saveCounts){
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.gson = gson;
		this.publisherDao = publisherDao;
		this.flushQueue = new ArrayBlockingQueue<>(60);//careful, size() must iterate every element
		this.flushScheduler = flushScheduler;
		this.saveCounts = saveCounts;

		this.flushScheduler.scheduleWithFixedDelay(this::flush, 0, 1, TimeUnit.SECONDS);
	}

	public void saveCounts(Map<Long,Map<String,Long>> counts){
		if(!flushQueue.offer(counts)){
			logger.warn("flushQueue rejected our counts");
		}
	}

	private void flush(){
		try{
			if(!saveCounts.get()){
				flushQueue.clear();
				return;
			}
			while(true){
				Map<Long,Map<String,Long>> counts = flushQueue.peek();
				if(counts == null){
					return;
				}
				var dto = new CountBatchDto(serviceName, serverName, counts);
				var json = gson.toJson(dto);
				var message = new ConveyorMessage(UlidTool.nextUlid(), json);
				logCountsSpec(counts, json);
				publisherDao.put(message);
				flushQueue.poll();
			}
		}catch(Throwable e){
			logger.warn("", e);
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
		private DatarouterCountPublisherDao publisherDao;
		@Inject
		private DatarouterCountFlushSchedulerExecutor flushScheduler;
		@Inject
		private DatarouterCountSettingRoot settings;

		public CountFlusher create(String serviceName, String serverName){
			return new CountFlusher(serviceName, serverName, gson, publisherDao, flushScheduler, settings.saveCounts);
		}

	}

}
