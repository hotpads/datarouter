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
package io.datarouter.trace.conveyor;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.trace.TraceEntityDto;

public class TraceToFilterBufferTool{
	private static final Logger logger = LoggerFactory.getLogger(TraceToFilterBufferTool.class);

	public static Optional<String> offerDtoToBuffer(boolean shouldRun, MemoryBuffer<TraceEntityDto> buffer,
			TraceEntityDto entityDto){
		if(!shouldRun){
			return Optional.empty();
		}
		if(!buffer.offer(entityDto)){
			logger.warn("error offering trace entity buffer={} traceId={}", buffer.getName(), entityDto.traceDto
					.getTraceId());
			return Optional.empty();
		}
		return Optional.of(buffer.getName());
	}

}
