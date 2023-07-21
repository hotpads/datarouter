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
package io.datarouter.conveyor;

import io.datarouter.storage.metric.Gauges;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConveyorGauges implements ConveyorGaugeRecorder{

	private static final String PREFIX = "Conveyor";

	@Inject
	private Gauges gauges;

	@Override
	public void savePeekDurationMs(ConveyorRunnable conveyor, long value){
		gauges.save(PREFIX + " peek duration ms", value);
		gauges.save(PREFIX + " " + conveyor.getName() + " peek duration ms", value);
	}

	@Override
	public void saveAckDurationMs(ConveyorRunnable conveyor, long value){
		gauges.save(PREFIX + " ack duration ms", value);
		gauges.save(PREFIX + " " + conveyor.getName() + " ack duration ms", value);
	}

	@Override
	public void savePeekToProcessBufferDurationMs(ConveyorRunnable conveyor, long value){
		gauges.save(PREFIX + " peek to process buffer duration ms", value);
		gauges.save(PREFIX + " " + conveyor.getName() + " peek to process buffer duration ms", value);
	}

	@Override
	public void saveProcessBufferDurationMs(ConveyorRunnable conveyor, long value){
		gauges.save(PREFIX + " process buffer duration ms", value);
		gauges.save(PREFIX + " " + conveyor.getName() + " process buffer duration ms", value);
	}

}
