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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGaugeRecorder;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.web.exception.ExceptionRecorder;

public class DatabeanBufferConveyor<D> extends BaseConveyor{

	private static final int BATCH_SIZE = 100;

	private final MemoryBuffer<D> memoryBuffer;
	private final Consumer<Collection<D>> putMultiConsumer;

	public DatabeanBufferConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<D> memoryBuffer,
			Consumer<Collection<D>> putMultiConsumer,
			ExceptionRecorder exceptionRecorder,
			ConveyorGaugeRecorder gaugeRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder, gaugeRecorder);
		this.memoryBuffer = memoryBuffer;
		this.putMultiConsumer = putMultiConsumer;
	}

	@Override
	public ProcessBatchResult processBatch(){
		Instant beforePeek = Instant.now();
		List<D> databeans = memoryBuffer.pollMultiWithLimit(BATCH_SIZE);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(databeans.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			Instant beforeProcessBuffer = Instant.now();
			putMultiConsumer.accept(databeans);
			Instant afterProcessBuffer = Instant.now();
			gaugeRecorder.saveProcessBufferDurationMs(this, Duration.between(beforeProcessBuffer, afterProcessBuffer)
					.toMillis());
			ConveyorCounters.incPutMultiOpAndDatabeans(this, databeans.size());
			return new ProcessBatchResult(true);
		}catch(RuntimeException putMultiException){
			databeans.forEach(memoryBuffer::offer);// might as well try to save them for later
			ConveyorCounters.inc(this, "putMulti exception", 1);
			throw putMultiException;
		}
	}

}
