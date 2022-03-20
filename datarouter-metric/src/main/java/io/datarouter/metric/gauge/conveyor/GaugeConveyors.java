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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.metric.gauge.DatarouterGaugePublisherDao;
import io.datarouter.metric.gauge.GaugeBlobService;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class GaugeConveyors extends BaseConveyors{

	@Inject
	private DatarouterGaugePublisherDao dao;
	@Inject
	private DatarouterGaugeSettingRoot settings;
	@Inject
	private Gson gson;
	@Inject
	private GaugePublisher publisher;
	@Inject
	private GaugeBuffers buffers;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private GaugeBlobService gaugeBlobService;

	@Override
	public void onStartUp(){
		start(new GaugeMemoryToQueueConveyor(
				"gaugeMemoryToQueue",
				settings.runGaugeMemoryToQueue,
				settings,
				dao::putMulti,
				buffers.gaugeBuffer,
				gson,
				exceptionRecorder,
				gaugeBlobService),
				settings.memoryConveyorThreadCount.get());
		start(new GaugeQueueDrainConveyor(
				"gaugeQueueToPublisher",
				settings.runGaugeQueueToPublisher,
				dao.getGroupQueueConsumer(),
				gson,
				publisher,
				settings.compactExceptionLoggingForConveyors,
				exceptionRecorder),
				settings.drainConveyorThreadCount.get());
	}

}
