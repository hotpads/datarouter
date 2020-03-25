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
package io.datarouter.metric.counter;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.metric.counter.collection.DatarouterCountCollector;
import io.datarouter.metric.counter.collection.archive.CountArchiveFlusher;
import io.datarouter.metric.counter.collection.archive.CountArchiveFlusherFactory;
import io.datarouter.metric.counter.collection.archive.CountPartitions;
import io.datarouter.metric.counter.collection.archive.imp.MemoryCountArchive;
import io.datarouter.metric.counter.collection.archive.imp.WritableDatabeanCountArchive;
import io.datarouter.metric.counter.setting.DatarouterCountSettingRoot;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.web.listener.DatarouterAppListener;

@Singleton
public class CountersAppListener implements DatarouterAppListener{

	private static final long ROLL_PERIOD_MS = 1_000;

	private final CountArchiveFlusherFactory flusherFactory;
	private final DatarouterCountPublisherDao countPublisherDao;
	private final Gson gson;
	private final DatarouterCountSettingRoot settings;

	private final String serverName;
	private final String serviceName;

	@Inject
	public CountersAppListener(DatarouterProperties datarouterProperties, DatarouterService datarouterService,
			CountArchiveFlusherFactory flusherFactory, DatarouterCountPublisherDao countPublisherDao, Gson gson,
			DatarouterCountSettingRoot settings){
		this.serverName = datarouterProperties.getServerName();
		this.serviceName = datarouterService.getName();
		this.flusherFactory = flusherFactory;
		this.countPublisherDao = countPublisherDao;
		this.gson = gson;
		this.settings = settings;
	}

	// add flushers to a collector and register it with the global Counters class
	@Override
	public void onStartUp(){
		DatarouterCountCollector collector = new DatarouterCountCollector(ROLL_PERIOD_MS);

		// memory flushers
		String nameMemory = serviceName + "_memory";
		CountArchiveFlusher memoryFlusher = flusherFactory.createMemoryFlusher(nameMemory, 1000);
		loadMemoryCounters(memoryFlusher);
		memoryFlusher.start();
		collector.addFlusher(memoryFlusher);

		// database flushers
		String nameDatabase = serviceName + "_" + "database";
		CountArchiveFlusher dbFlusher = flusherFactory.createDbFlusher(nameDatabase, ROLL_PERIOD_MS);
		loadDbCounters(dbFlusher);
		dbFlusher.start();
		collector.addFlusher(dbFlusher);

		// register the datarouter count collector to receive counts throughout the app
		Counters.addCollector(collector);
	}

	@Override
	public void onShutDown(){
		Counters.stopAndFlushAll();
	}

	private void loadMemoryCounters(CountArchiveFlusher memoryFlusher){
		var primaryArchive = new MemoryCountArchive(serviceName, serverName, 1 * ROLL_PERIOD_MS, 600);// 10m
		memoryFlusher.addArchive(primaryArchive);

		var memArchive5Seconds = new MemoryCountArchive(serviceName, serverName, 5 * ROLL_PERIOD_MS, 720);// 1h
		memoryFlusher.addArchive(memArchive5Seconds);

		var memArchive20Seconds = new MemoryCountArchive(serviceName, serverName, 20 * ROLL_PERIOD_MS, 1440);// 8h
		memoryFlusher.addArchive(memArchive20Seconds);

		var memArchive1Minute = new MemoryCountArchive(serviceName, serverName, 60 * ROLL_PERIOD_MS, 2880);// 2d
		memoryFlusher.addArchive(memArchive1Minute);
	}

	private void loadDbCounters(CountArchiveFlusher dbFlusher){
		Arrays.stream(CountPartitions.values())
				.map(period -> new WritableDatabeanCountArchive(
						serviceName,
						serverName,
						period.getPeriodMs(),
						period.getFlushPeriodMs(),
						countPublisherDao,
						gson,
						settings))
				.forEach(dbFlusher::addArchive);
	}

}
