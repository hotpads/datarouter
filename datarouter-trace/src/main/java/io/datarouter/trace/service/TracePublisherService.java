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
package io.datarouter.trace.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.storage.TraceDirectoryDao;
import io.datarouter.trace.storage.TraceQueueDao;
import io.datarouter.trace.storage.binarydto.TraceBinaryDto;
import io.datarouter.types.Ulid;

@Singleton
public class TracePublisherService implements TracePublisher{
	private static final Logger logger = LoggerFactory.getLogger(TracePublisherService.class);

	private final DatarouterTracePublisherSettingRoot traceSettings;

	private final TraceDirectoryDao traceDirectoryDao;
	private final TraceQueueDao traceQueueDao;

	@Inject
	public TracePublisherService(
			DatarouterTracePublisherSettingRoot traceSettings,
			TraceDirectoryDao traceDirectoryDao,
			TraceQueueDao traceQueueDao){
		this.traceSettings = traceSettings;
		this.traceDirectoryDao = traceDirectoryDao;
		this.traceQueueDao = traceQueueDao;
	}

	@Override
	public PublishingResponseDto addBatch(Trace2BatchedBundleDto traceBatchedDto){
		var isQueue = traceSettings.saveTracesToQueueDaoInsteadOfDirectoryDao.get();
		logger.info(
				"writing size={} traces to {}",
				traceBatchedDto.batch.size(),
				isQueue ? "queue" : "directory");
		var traces = Scanner.of(traceBatchedDto.batch)
				.map(TraceBinaryDto::new);
		if(isQueue){
			traceQueueDao.combineAndPut(traces);
		}else{
			traceDirectoryDao.write(traces, new Ulid());
		}
		return PublishingResponseDto.SUCCESS;
	}

}
