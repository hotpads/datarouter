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

import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.trace.conveyor.BaseTrace2HttpRequestRecordSqsDrainConveyor;
import io.datarouter.web.exception.ExceptionRecorder;

public class Trace2ForLocalHttpRequestRecordSqsDrainConveyor extends BaseTrace2HttpRequestRecordSqsDrainConveyor{

	public Trace2ForLocalHttpRequestRecordSqsDrainConveyor(String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, groupQueueConsumer, compactExceptionLogging, gson, exceptionRecorder);
	}

	@Override
	public void persistData(HttpRequestRecordBatchDto batchDto){
		/*
		 * TODO for saving HttpRequestRecord on local, we need to create a local publisher that saves records to
		 * DatarouterHttpRequestRecordDao
		 *
		 * */
	}

}
