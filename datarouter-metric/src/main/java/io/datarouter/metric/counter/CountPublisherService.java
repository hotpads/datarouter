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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.metric.counter.collection.DatarouterCountCollector.CountCollectorStats;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;

@Singleton
public class CountPublisherService implements CountPublisher{
	private static final Logger logger = LoggerFactory.getLogger(CountPublisherService.class);

	private final CountDirectoryDao countDirectoryDao;
	private final CountQueueDao countQueueDao;
	private final DatarouterCountSettingRoot countSettings;
	private final ServiceName serviceName;
	private final ServerName serverName;

	@Inject
	public CountPublisherService(CountDirectoryDao countDirectoryDao, CountQueueDao countQueueDao,
			DatarouterCountSettingRoot countSettings, ServiceName serviceName, ServerName serverName){
		this.countDirectoryDao = countDirectoryDao;
		this.countQueueDao = countQueueDao;
		this.countSettings = countSettings;
		this.serviceName = serviceName;
		this.serverName = serverName;
	}

	@Override
	public PublishingResponseDto publishStats(Map<Long,Map<String,CountCollectorStats>> counts){
		boolean isQueue = countSettings.saveCountStatsToQueueDaoInsteadOfDirectoryDao.get();
		String ulid = new Ulid().value();
		var dtos = CountBinaryDto.createSizedCountBinaryDtos(
				ulid,
				serviceName.get(),
				serverName.get(),
				counts,
				isQueue ? 100 : Integer.MAX_VALUE);
		logger.info(
				"writing size={} CountBinaryDtos with key={} to {}",
				dtos.size(),
				ulid,
				isQueue ? "queue" : "directory");
		if(isQueue){
			countQueueDao.combineAndPut(dtos);
		}else{
			countDirectoryDao.write(dtos.get(0), ulid);
		}
		return PublishingResponseDto.SUCCESS;
	}

}
