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
package io.datarouter.trace.conveyor.local;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.BaseDatarouterTraceDao;
import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.trace.Trace;

public class TraceSqsDrainConveyor extends BaseGroupQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{

	private final Gson gson;
	private final BaseDatarouterTraceDao traceDao;

	public TraceSqsDrainConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			BaseDatarouterTraceDao traceDao,
			Gson gson){
		super(name, shouldRun, groupQueueConsumer, () -> false, Duration.ofSeconds(30));
		this.gson = gson;
		this.traceDao = traceDao;
	}

	@Override
	protected void processDatabeans(List<ConveyorMessage> traceEntity){
		traceEntity.forEach(this::persistEntity);
	}

	private void persistEntity(ConveyorMessage message){
		TraceEntityDto dto = gson.fromJson(message.getMessage(), TraceEntityDto.class);
		Trace trace = new Trace(dto.traceDto);
		List<TraceThread> threads = Scanner.of(dto.traceThreadDtos).map(TraceThread::new).list();
		List<TraceSpan> spans = Scanner.of(dto.traceSpanDtos).map(TraceSpan::new).list();
		traceDao.putMulti(threads, spans, trace);
	}

}
