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
package io.datarouter.conveyor.queue.configuration;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorConfigurationGroupService;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.consumer.QueueConsumer;
import jakarta.inject.Inject;

public abstract class BaseQueueConsumerConveyorConfiguration<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(BaseQueueConsumerConveyorConfiguration.class);

	@Inject
	private ConveyorGauges gaugeRecorder;
	@Inject
	private Gson gson;
	@Inject
	private ConveyorConfigurationGroupService conveyorConfigurationGroupService;

	protected abstract void processOne(D databean);
	protected abstract QueueConsumer<PK,D> getQueueConsumer();

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		QueueMessage<PK,D> message = getQueueConsumer().peek(DEFAULT_PEEK_TIMEOUT, DEFAULT_VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", conveyor.getName());
			return new ProcessResult(false);
		}
		D databean = message.getDatabean();
		logger.info("peeked conveyor={} messageCount={}", conveyor.getName(), 1);
		Instant beforeProcessBuffer = Instant.now();
		try{
			if(!processOneShouldAck(databean)){
				return new ProcessResult(true);
			}
		}catch(Exception e){
			throw new RuntimeException("databean=" + gson.toJson(databean), e);
		}
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(
				conveyor,
				Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis());
		if(Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis() > getVisibilityTimeout().toMillis()){
			logger.warn(
					"slow conveyor conveyor={} durationMs={} databean={}",
					conveyor.getName(),
					Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis(),
					databean);
		}
		logger.info("consumed conveyor={} messageCount={}", conveyor.getName(), 1);
		ConveyorCounters.incConsumedOpAndDatabeans(conveyor, 1);
		Instant beforeAck = Instant.now();
		getQueueConsumer().ack(message.getKey());
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(conveyor, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", conveyor.getName(), 1);
		ConveyorCounters.incAck(conveyor);
		return new ProcessResult(true);
	}

	public String getName(){
		return conveyorConfigurationGroupService.getConveyorName(this.getClass());
	}

	protected Duration getVisibilityTimeout(){
		return DEFAULT_VISIBILITY_TIMEOUT;
	}

	protected boolean processOneShouldAck(D databean){
		processOne(databean);
		return true;
	}

}
