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
package io.datarouter.metric.gauge.conveyor;

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
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.metric.gauge.GaugeBlobService;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class GaugeMemoryToQueueConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(GaugeMemoryToQueueConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final DatarouterGaugeSettingRoot settings;
	private final Consumer<Collection<ConveyorMessage>> putMultiConsumer;
	private final MemoryBuffer<GaugeDto> buffer;
	private final Gson gson;
	private final GaugeBlobService gaugeBlobService;

	public GaugeMemoryToQueueConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			DatarouterGaugeSettingRoot settings,
			Consumer<Collection<ConveyorMessage>> putMultiConsumer,
			MemoryBuffer<GaugeDto> buffer,
			Gson gson,
			ExceptionRecorder exceptionRecorder,
			GaugeBlobService gaugeBlobService){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.settings = settings;
		this.putMultiConsumer = putMultiConsumer;
		this.buffer = buffer;
		this.gson = gson;
		this.gaugeBlobService = gaugeBlobService;
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<GaugeDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			boolean shouldBufferInQueue = settings.sendGaugesFromMemoryToQueue.get();
			if(settings.saveGaugeBlobsAndOverrideDtos.get()){
				try{
					gaugeBlobService.add(new GaugeBatchDto(dtos));
					shouldBufferInQueue = false;//avoid writing to both blob and DTO queue
				}catch(RuntimeException e){
					logger.warn("", e);
				}
			}
			if(shouldBufferInQueue){
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
