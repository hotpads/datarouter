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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.trace.conveyor.TraceToFilterBufferTool;
import io.datarouter.trace.settings.DatarouterTraceLocalSettingRoot;

@Singleton
public class TraceLocalFilterToMemoryBuffer implements FilterToMemoryBufferForLocal{

	private static final int MAX_TRACES = 1_000;

	public final MemoryBuffer<TraceEntityDto> buffer;
	private final DatarouterTraceLocalSettingRoot settings;

	@Inject
	public TraceLocalFilterToMemoryBuffer(DatarouterTraceLocalSettingRoot settings){
		this.settings = settings;
		this.buffer = new MemoryBuffer<>("localTraceBuffer", MAX_TRACES);
	}

	@Override
	public Optional<String> offer(TraceEntityDto dto){
		return TraceToFilterBufferTool.offerDtoToBuffer(settings.runMemoryToSqs.get(), buffer, dto);
	}

}
