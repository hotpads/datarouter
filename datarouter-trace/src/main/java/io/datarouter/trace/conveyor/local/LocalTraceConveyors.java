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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.trace.conveyor.Trace2MemoryBufferToSqsConveyor;
import io.datarouter.trace.settings.DatarouterTraceLocalSettingRoot;
import io.datarouter.trace.storage.Trace2ForLocalDao;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.Java9Gson;

@Singleton
public class LocalTraceConveyors extends BaseConveyors{

	@Inject
	private DatarouterTraceLocalSettingRoot settings;
	@Inject
	private Gson gson;
	@Inject
	@Java9Gson
	private Gson java9Gson;
	@Inject
	private Trace2ForLocalFilterToMemoryBuffer trace2MemoryBuffer;
	@Inject
	private Trace2ForLocalQueueDao trace2QueueDao;
	@Inject
	private Trace2ForLocalHttpRequestRecordQueueDao trace2HttpRequestRecordQueueDao;
	@Inject
	private Trace2ForLocalDao trace2Dao;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	@Override
	public void onStartUp(){
		start(new Trace2MemoryBufferToSqsConveyor(
				"trace2LocalMemoryToSqs",
				settings.runMemoryToSqsForTrace2,
				settings.bufferInSqsForTrace2,
				trace2MemoryBuffer.buffer,
				trace2QueueDao,
				trace2HttpRequestRecordQueueDao,
				java9Gson,
				exceptionRecorder),
				1);

		start(new Trace2ForLocalSqsDrainConveyor(
				"trace2SqsToLocalStorage",
				settings.drainSqsToLocalForTrace2,
				trace2QueueDao.getGroupQueueConsumer(),
				gson,
				trace2Dao,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);

		start(new Trace2ForLocalHttpRequestRecordSqsDrainConveyor(
				"trace2HttpRequestRecordSqsToLocalStorage",
				settings.drainSqsToLocalForTrace2HttpRequestRecord,
				trace2HttpRequestRecordQueueDao.getGroupQueueConsumer(),
				gson,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);
	}

}
