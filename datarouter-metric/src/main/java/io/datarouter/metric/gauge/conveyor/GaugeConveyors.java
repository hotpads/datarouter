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

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class GaugeConveyors extends BaseConveyors{

	@Inject
	private DatarouterGaugeSettingRoot settings;
	@Inject
	private GaugeBuffers buffers;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private GaugePublisher gaugePublisher;

	@Override
	public void onStartUp(){
		start(new GaugeMemoryToPublisherConveyor(
				"gaugeMemoryToPublisher",
				settings.runGaugeMemoryToPublisherConveyor,
				buffers.gaugeBuffer,
				exceptionRecorder,
				gaugePublisher),
				settings.conveyorThreadCount.get());
	}

}
