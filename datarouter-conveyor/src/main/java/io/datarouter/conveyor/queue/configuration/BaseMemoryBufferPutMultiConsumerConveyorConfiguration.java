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
package io.datarouter.conveyor.queue.configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.buffer.MemoryBuffer;
import jakarta.inject.Inject;

public abstract class BaseMemoryBufferPutMultiConsumerConveyorConfiguration<D>
implements ConveyorConfiguration{

	private static final int DEFAULT_BATCH_SIZE = 100;

	@Inject
	private ConveyorGauges gaugeRecorder;

	protected abstract MemoryBuffer<D> getMemoryBuffer();
	protected abstract Consumer<Collection<D>> getPutMultiConsumer();

	protected int getBatchSize(){
		return DEFAULT_BATCH_SIZE;
	}

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		List<D> databeans = getMemoryBuffer().pollMultiWithLimit(getBatchSize());
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(databeans.isEmpty()){
			return new ProcessResult(false);
		}
		try{
			Instant beforeProcessBuffer = Instant.now();
			getPutMultiConsumer().accept(databeans);
			Instant afterProcessBuffer = Instant.now();
			gaugeRecorder.saveProcessBufferDurationMs(
					conveyor,
					Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis());
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, databeans.size());
			return new ProcessResult(true);
		}catch(RuntimeException putMultiException){
			databeans.forEach(getMemoryBuffer()::offer);// might as well try to save them for later
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
			throw putMultiException;
		}
	}

}
