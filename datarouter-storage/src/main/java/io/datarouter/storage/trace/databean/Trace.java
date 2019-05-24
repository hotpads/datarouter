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
package io.datarouter.storage.trace.databean;

import io.datarouter.instrumentation.trace.TraceDto;

public class Trace extends BaseTrace<TraceEntityKey,TraceKey,Trace>{

	public Trace(){
		this.key = new TraceKey();
	}

	public Trace(String traceId){
		this.key = new TraceKey(traceId);
	}

	public Trace(TraceDto dto){
		super(dto);
		this.key = new TraceKey(dto.getTraceId());
	}

	public static class TraceFielder extends BaseTraceFielder<TraceEntityKey,TraceKey,Trace>{

		public TraceFielder(){
			super(TraceKey.class);
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
				getDuration());
	}

}
