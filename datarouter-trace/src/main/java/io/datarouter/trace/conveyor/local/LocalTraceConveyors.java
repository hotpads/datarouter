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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.trace.conveyor.TraceMemoryToSqsConveyor;
import io.datarouter.trace.settings.DatarouterTraceLocalSettingRoot;
import io.datarouter.trace.storage.BaseDatarouterTraceDao;
import io.datarouter.trace.storage.BaseDatarouterTraceQueueDao;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class LocalTraceConveyors extends BaseConveyors{

	@Inject
	private DatarouterTraceLocalSettingRoot settings;
	@Inject
	private Gson gson;
	@Inject
	private TraceLocalFilterToMemoryBuffer memoryBuffer;
	@Inject
	private BaseDatarouterTraceQueueDao traceQueueDao;
	@Inject
	private BaseDatarouterTraceDao traceDao;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	@Override
	public void onStartUp(){
		start(new TraceMemoryToSqsConveyor(
				"traceMemoryToSqs",
				settings.runMemoryToSqs,
				settings.bufferInSqs,
				memoryBuffer.buffer,
				traceQueueDao,
				traceDao,
				gson,
				exceptionRecorder),
				1);
		start(new TraceSqsDrainConveyor(
				"traceSqsToLocalStorage",
				settings.drainSqsToLocal,
				traceQueueDao.getGroupQueueConsumer(),
				traceDao,
				gson,
				exceptionRecorder),
				1);
	}

}
