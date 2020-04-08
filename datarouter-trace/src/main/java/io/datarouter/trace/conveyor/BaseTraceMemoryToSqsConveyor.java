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
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.util.iterable.IterableTool;

public abstract class BaseTraceMemoryToSqsConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseTraceMemoryToSqsConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final MemoryBuffer<TraceEntityDto> buffer;
	private final Gson gson;

	public BaseTraceMemoryToSqsConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<TraceEntityDto> buffer,
			Gson gson){
		super(name, shouldRun, () -> false);
		this.buffer = buffer;
		this.gson = gson;
	}

	public abstract void processTraceEntityDtos(List<TraceEntityDto> dtos);

	@Override
	public ProcessBatchResult processBatch(){
		List<TraceEntityDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			processTraceEntityDtos(dtos);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
			return new ProcessBatchResult(true);
		}catch(RuntimeException putMultiException){
			List<String> ids = IterableTool.nullSafeMap(dtos, dto -> dto.traceDto.getTraceId());
			logger.warn("exception sending trace to sqs ids={}", ids, putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);//backoff for a bit
		}
	}

	protected ConveyorMessage toMessage(TraceEntityDto dto){
		return new ConveyorMessage(dto.traceDto.getTraceId(), gson.toJson(dto));
	}

}