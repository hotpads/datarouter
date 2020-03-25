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
package io.datarouter.trace.storage.trace;

import java.time.Duration;

import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.model.serialize.fielder.TtlFielderConfig;
import io.datarouter.trace.storage.entity.TraceEntityKey;

public class Trace extends BaseTrace<TraceEntityKey,TraceKey,Trace>{

	public static final Duration TTL = Duration.ofDays(30);
	public static final TtlFielderConfig TTL_FIELDER_CONFIG = new TtlFielderConfig(TTL);

	public Trace(){
		super(new TraceKey());
	}

	public Trace(String traceId){
		super(new TraceKey(traceId));
	}

	public Trace(TraceDto dto){
		super(new TraceKey(dto.getTraceId()), dto);
	}

	public static class TraceFielder extends BaseTraceFielder<TraceEntityKey,TraceKey,Trace>{

		public TraceFielder(){
			super(TraceKey.class);
			addOption(TTL_FIELDER_CONFIG);
		}

	}

	@Override
	public Class<TraceKey> getKeyClass(){
		return TraceKey.class;
	}

	@Override
	public TraceDto toDto(){
		return new TraceDto(
				getTraceId(),
				getContext(),
				getType(),
				getParams(),
				getCreated(),
				getDuration(),
				getDiscardedThreadCount());
	}

}
