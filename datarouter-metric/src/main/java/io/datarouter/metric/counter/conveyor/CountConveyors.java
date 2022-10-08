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
package io.datarouter.metric.counter.conveyor;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class CountConveyors extends BaseConveyors{

	@Inject
	private DatarouterCountSettingRoot settings;
	@Inject
	private CountBuffers buffers;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private CountPublisher countPublisher;
	@Inject
	private ConveyorGauges conveyorGauges;

	@Override
	public void onStartUp(){
		start(new CountMemoryToPublisherConveyor(
				"countMemoryToPublisher",
				settings.runCountMemoryToPublisherConveyor,
				buffers.countBuffer,
				exceptionRecorder,
				countPublisher,
				conveyorGauges),
				settings.conveyorThreadCount.get(),
				1);
	}

}