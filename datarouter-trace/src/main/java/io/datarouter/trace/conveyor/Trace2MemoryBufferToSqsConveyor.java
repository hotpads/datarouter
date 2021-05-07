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
package io.datarouter.trace.conveyor;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.BaseTrace2HttpRequestRecordQueueDao;
import io.datarouter.trace.storage.BaseTraceQueueDao;
import io.datarouter.web.exception.ExceptionRecorder;

public class Trace2MemoryBufferToSqsConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(Trace2MemoryBufferToSqsConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final Supplier<Boolean> shouldBufferInSqs;
	private final BaseTraceQueueDao traceQueueDao;
	private final BaseTrace2HttpRequestRecordQueueDao traceHttpRequestRecordQueueDao;
	private final MemoryBuffer<Trace2BundleAndHttpRequestRecordDto> buffer;
	private final Gson gson;

	public Trace2MemoryBufferToSqsConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			Supplier<Boolean> shouldBufferInSqs,
			MemoryBuffer<Trace2BundleAndHttpRequestRecordDto> buffer,
			BaseTraceQueueDao traceQueueDao,
			BaseTrace2HttpRequestRecordQueueDao traceHttpReqeustRecordDao,
			Gson gson,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.shouldBufferInSqs = shouldBufferInSqs;
		this.traceQueueDao = traceQueueDao;
		this.traceHttpRequestRecordQueueDao = traceHttpReqeustRecordDao;
		this.buffer = buffer;
		this.gson = gson;
	}

	public void processTraceEntityDtos(List<Trace2BundleAndHttpRequestRecordDto> dtos){
		if(shouldBufferInSqs.get()){
			Scanner.of(dtos).map(this::toTrace2Message).flush(traceQueueDao::putMulti);
			Scanner.of(dtos)
				.map(this::toHttpReqRecordMessage)
				.concat(OptionalScanner::of)
				.flush(traceHttpRequestRecordQueueDao::putMulti);
		}
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<Trace2BundleAndHttpRequestRecordDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			processTraceEntityDtos(dtos);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
			return new ProcessBatchResult(true);
		}catch(RuntimeException putMultiException){
			List<Traceparent> ids = Scanner.of(dtos)
					.map(Trace2BundleAndHttpRequestRecordDto::getTraceparent)
					.list();
			logger.warn("exception sending trace to sqs ids={}", ids, putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);//backoff for a bit
		}
	}

	protected ConveyorMessage toTrace2Message(Trace2BundleAndHttpRequestRecordDto dto){
		return new ConveyorMessage(dto.getTraceparent().toString(), gson.toJson(dto.traceBundleDto));
	}

	protected Optional<ConveyorMessage> toHttpReqRecordMessage(Trace2BundleAndHttpRequestRecordDto dto){
		// when there's an exception, httpRequestRecord is recorded through exceptionRecorder
		if(dto.httpRequestRecord == null){
			return Optional.empty();
		}
		return Optional.of(new ConveyorMessage(dto.getTraceparent().toString(), gson.toJson(dto.httpRequestRecord)));
	}

}
