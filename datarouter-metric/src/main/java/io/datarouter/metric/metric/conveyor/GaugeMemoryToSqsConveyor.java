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
package io.datarouter.metric.metric.conveyor;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;

public class GaugeMemoryToSqsConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(GaugeMemoryToSqsConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final Setting<Boolean> shouldBufferInSqs;
	private final Consumer<Collection<ConveyorMessage>> putMultiConsumer;
	private final MemoryBuffer<GaugeDto> buffer;
	private final Gson gson;

	public GaugeMemoryToSqsConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			Setting<Boolean> shouldBufferInSqs,
			Consumer<Collection<ConveyorMessage>> putMultiConsumer,
			MemoryBuffer<GaugeDto> buffer,
			Gson gson){
		super(name, shouldRun, () -> false);
		this.shouldBufferInSqs = shouldBufferInSqs;
		this.putMultiConsumer = putMultiConsumer;
		this.buffer = buffer;
		this.gson = gson;
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<GaugeDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			if(shouldBufferInSqs.get()){
				Scanner.of(dtos)
						.map(this::toConveyorMessage)
						.flush(putMultiConsumer::accept);
			}
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
			return new ProcessBatchResult(true);
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);// backoff for a bit
		}
	}

	private ConveyorMessage toConveyorMessage(GaugeDto dto){
		return new ConveyorMessage(dto.name, gson.toJson(dto));
	}

}
