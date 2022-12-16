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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;

@Singleton
public class GaugeMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(GaugeMemoryToPublisherConveyorConfiguration.class);
	//this only controls max buffer poll size. publisher will split into as many messages as necessary
	private static final int BATCH_SIZE = 5_000;

	@Inject
	private DatarouterGaugeSettingRoot settings;
	@Inject
	private GaugeBuffers buffers;
	@Inject
	private GaugePublisher gaugePublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		List<GaugeDto> dtos = buffers.gaugeBuffer.pollMultiWithLimit(BATCH_SIZE);
		Instant afterPeek = Instant.now();
		if(settings.recordGaugeMemoryToPublisherGauges.get()){
			gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		}
		if(dtos.isEmpty()){
			return new ProcessResult(false);
		}
		try{
			gaugePublisher.publish(new GaugeBatchDto(dtos));
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, dtos.size());
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
		}
		//process as many as possible if shutting down
		return new ProcessResult(conveyor.isShuttingDown() || dtos.size() == BATCH_SIZE);
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}
