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
package io.datarouter.trace.conveyor.local;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.conveyor.BaseTrace2SqsDrainConveyor;
import io.datarouter.trace.storage.Trace2ForLocalDao;
import io.datarouter.trace.storage.span.Trace2Span;
import io.datarouter.trace.storage.thread.Trace2Thread;
import io.datarouter.trace.storage.trace.Trace2;
import io.datarouter.web.exception.ExceptionRecorder;

public class Trace2ForLocalSqsDrainConveyor extends BaseTrace2SqsDrainConveyor{

	private final Trace2ForLocalDao traceDao;

	public Trace2ForLocalSqsDrainConveyor(String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			Trace2ForLocalDao traceDao,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, groupQueueConsumer, compactExceptionLogging, gson, exceptionRecorder);
		this.traceDao = traceDao;
	}

	@Override
	public void persistData(Trace2BatchedBundleDto batchDto){
		for(Trace2BundleDto dto : batchDto.batch){
			List<Trace2> traces = new ArrayList<>();
			if(dto.traceDto != null){
				traces.add(new Trace2(Trace2.DEFAULT_ACCOUNT_NAME, dto.traceDto));
			}
			List<Trace2Thread> threads = Scanner.of(dto.traceThreadDtos)
					.map(Trace2Thread::new)
					.list();
			List<Trace2Span> spans = Scanner.of(dto.traceSpanDtos)
					.map(Trace2Span::new)
					.list();
			traceDao.putMultiTraceBundles(threads, spans, traces);
		}
	}

}
