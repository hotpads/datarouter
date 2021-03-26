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
package io.datarouter.trace.conveyor.publisher;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.trace.conveyor.Trace2MemoryBufferToSqsConveyor;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class TracePublisherConveyors extends BaseConveyors{

	@Inject
	private DatarouterTracePublisherSettingRoot settings;
	@Inject
	private Gson gson;
	@Inject
	private TracePublisher tracePublisher;
	@Inject
	private ExceptionRecordPublisher httpRequstRecordPublisher;
	@Inject
	private TracePublisherFilterToMemoryBuffer memoryBuffer;
	@Inject
	private Trace2ForPublisherFilterToMemoryBuffer trace2MemoryBuffer;
	@Inject
	private TraceQueuePublisherDao traceQueueDao;
	@Inject
	private Trace2ForPublisherQueueDao trace2QueueDao;
	@Inject
	private Trace2ForPublisherHttpRequestRecordQueueDao trace2HttpRequestRecordQueueDao;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	@Override
	public void onStartUp(){
		start(new TraceMemoryToSqsConveyorPublishing(
				"traceMemoryToSqsPublisher",
				settings.runMemoryToSqs,
				settings.bufferInSqs,
				memoryBuffer.buffer,
				traceQueueDao::putMulti,
				gson,
				exceptionRecorder),
				1);
		start(new TraceSqsDrainConveyorPublisher(
				"traceSqsToPublisher",
				settings.drainSqsToPublisher,
				traceQueueDao.getGroupQueueConsumer(),
				gson,
				tracePublisher,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);

		/***** trace2 *****/
		start(new Trace2MemoryBufferToSqsConveyor(
				"trace2MemoryToSqsPublisher",
				settings.runMemoryToSqsForTrace2,
				settings.bufferInSqsForTrace2,
				trace2MemoryBuffer.buffer,
				trace2QueueDao,
				trace2HttpRequestRecordQueueDao,
				gson,
				exceptionRecorder),
				1);

		start(new Trace2ForPublisherSqsDrainConveyor(
				"trace2SqsToPublisher",
				settings.drainSqsToPublisherForTrace2,
				trace2QueueDao.getGroupQueueConsumer(),
				gson,
				tracePublisher,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);

		start(new Trace2ForPublisherHttpRequestRecordSqsDrainConveyor(
				"trace2HttpRequestRecordSqsToPublisher",
				settings.drainSqsToPublisherForTrace2HttpRequestRecord,
				trace2HttpRequestRecordQueueDao.getGroupQueueConsumer(),
				gson,
				httpRequstRecordPublisher,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				1);
	}

}
