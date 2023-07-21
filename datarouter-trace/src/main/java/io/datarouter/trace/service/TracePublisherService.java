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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.TraceQueueDao;
import io.datarouter.trace.storage.binarydto.TraceBinaryDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TracePublisherService implements TracePublisher{
	private static final Logger logger = LoggerFactory.getLogger(TracePublisherService.class);

	private final TraceQueueDao traceQueueDao;

	@Inject
	public TracePublisherService(
			TraceQueueDao traceQueueDao){
		this.traceQueueDao = traceQueueDao;
	}

	@Override
	public PublishingResponseDto addBatch(Trace2BatchedBundleDto traceBatchedDto){
		logger.info("writing size={} traces to {}", traceBatchedDto.batch.size(), "queue");
		var traces = Scanner.of(traceBatchedDto.batch).map(TraceBinaryDto::new);
		traceQueueDao.combineAndPut(traces);
		return PublishingResponseDto.SUCCESS;
	}

}
