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

import io.datarouter.instrumentation.trace.TraceThreadDto;

public class TraceThread extends BaseTraceThread<TraceEntityKey,TraceThreadKey,TraceThread>{

	public TraceThread(){
		this.key = new TraceThreadKey(null, null);
	}

	public TraceThread(Long traceId, boolean hasParent){
		this.key = new TraceThreadKey(traceId, hasParent);
	}

	public TraceThread(TraceThreadDto dto){
		super(dto);
		this.key = new TraceThreadKey(dto.traceId, dto.threadId);
	}

	public static class TraceThreadFielder extends BaseTraceThreadFielder<TraceEntityKey,TraceThreadKey,TraceThread>{

		public TraceThreadFielder(){
			super(TraceThreadKey.class);
		}

	}

	public TraceThreadDto toDto(){
		return new TraceThreadDto(
				getKey().getTraceId(),
				getKey().getThreadId(),
				getParentId(),
				getName(),
				getInfo(),
				getServerId(),
				getCreated(),
				getQueuedDuration(),
				getRunningDuration(),
				getQueuedDurationNano(),
				getRunningDurationNano());
	}

	@Override
	public Class<TraceThreadKey> getKeyClass(){
		return TraceThreadKey.class;
	}

}
