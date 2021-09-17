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
package io.datarouter.metric.counter;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.service.ServiceName;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.counter.collection.CountFlusher;
import io.datarouter.metric.counter.collection.CountFlusher.CountFlusherFactory;
import io.datarouter.metric.counter.collection.CountPartitions;
import io.datarouter.metric.counter.collection.DatarouterCountCollector;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.web.listener.DatarouterAppListener;

@Singleton
public class CountersAppListener implements DatarouterAppListener{

	private static final long ROLL_PERIOD_MS = CountPartitions.PERIOD_5s.getPeriodMs();

	@Inject
	private DatarouterCountSettingRoot settings;
	@Inject
	private CountFlusherFactory flusherFactory;
	@Inject
	private ServerName serverName;
	@Inject
	private ServiceName serviceName;

	// add flushers to a collector and register it with the global Counters class
	@Override
	public void onStartUp(){
		CountFlusher flusher = flusherFactory.create(serviceName.get(), serverName.get());
		var collector = new DatarouterCountCollector(ROLL_PERIOD_MS, flusher, settings.runCountsToQueue);
		Counters.addCollector(collector);
	}

	@Override
	public void onShutDown(){
		Counters.stopAndFlushAll();
	}

}
