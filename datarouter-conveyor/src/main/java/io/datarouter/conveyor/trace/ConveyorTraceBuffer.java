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
package io.datarouter.conveyor.trace;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.config.DatarouterConveyorTraceSettings;
import io.datarouter.instrumentation.trace.ConveyorTraceAndTaskExecutorBundleDto;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.buffer.MemoryBuffer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConveyorTraceBuffer{
	private static final Logger logger = LoggerFactory.getLogger(ConveyorTraceBuffer.class);

	private static final int MAX_TRACES = 1_000;

	public final MemoryBuffer<ConveyorTraceAndTaskExecutorBundleDto> buffer;
	private final Setting<Boolean> shouldRunSetting;

	@Inject
	public ConveyorTraceBuffer(DatarouterConveyorTraceSettings settings){
		this.buffer = new MemoryBuffer<>("conveyorTraceBuffer", MAX_TRACES);
		this.shouldRunSetting = settings.saveTracesToMemory;
	}

	public Optional<String> offer(ConveyorTraceAndTaskExecutorBundleDto dto){
		if(!shouldRunSetting.get()){
			return Optional.empty();
		}
		if(!buffer.offer(dto)){
			logger.warn("error offering trace entity buffer={} traceparent={}",
					buffer.getName(),
					dto.traceBundleDto().traceDto.traceparent);
			return Optional.empty();
		}
		return Optional.of(buffer.getName());
	}


}
