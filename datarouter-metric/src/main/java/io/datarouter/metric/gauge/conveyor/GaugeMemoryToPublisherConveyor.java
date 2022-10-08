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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.web.exception.ExceptionRecorder;

public class GaugeMemoryToPublisherConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(GaugeMemoryToPublisherConveyor.class);

	//this only controls max buffer poll size. publisher will split into as many messages as necessary
	private static final int BATCH_SIZE = 5_000;

	private final MemoryBuffer<GaugeDto> buffer;
	private final GaugePublisher gaugePublisher;

	public GaugeMemoryToPublisherConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<GaugeDto> buffer,
			ExceptionRecorder exceptionRecorder,
			GaugePublisher gaugePublisher,
			ConveyorGauges conveyorGauges){
		super(name, shouldRun, () -> false, exceptionRecorder, conveyorGauges);
		this.buffer = buffer;
		this.gaugePublisher = gaugePublisher;
	}

	@Override
	public ProcessBatchResult processBatch(){
		Instant beforePeek = Instant.now();
		List<GaugeDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			gaugePublisher.publish(new GaugeBatchDto(dtos));
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
		}
		//process as many as possible if shutting down
		return new ProcessBatchResult(isShuttingDown() || dtos.size() == BATCH_SIZE);
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}
