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

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.util.iterable.IterableTool;

public class TraceSpan extends BaseTraceSpan<TraceEntityKey,TraceSpanKey,TraceSpan>{

	public TraceSpan(){
		this.key = new TraceSpanKey(null, null, null);
	}

	public TraceSpan(Long traceId, Long threadId, Integer sequence, Integer parentSequence){
		super(parentSequence);
		this.key = new TraceSpanKey(traceId, threadId, sequence);
	}

	public TraceSpan(TraceSpanDto dto){
		super(dto);
		this.key = new TraceSpanKey(dto.traceId, dto.threadId, dto.sequence);
	}

	public static class TraceSpanFielder extends BaseTraceSpanFielder<TraceEntityKey,TraceSpanKey,TraceSpan>{

		public TraceSpanFielder(){
			super(TraceSpanKey.class);
		}

	}

	public static SortedMap<TraceThreadKey,SortedSet<TraceSpan>> getByThreadKey(Iterable<TraceSpan> spans){
		SortedMap<TraceThreadKey,SortedSet<TraceSpan>> out = new TreeMap<>();
		for(TraceSpan span : IterableTool.nullSafe(spans)){
			TraceThreadKey threadKey = span.getThreadKey();
			if(out.get(threadKey) == null){
				out.put(threadKey, new TreeSet<TraceSpan>());
			}
			out.get(threadKey).add(span);
		}
		return out;
	}

	public TraceSpanDto toDto(){
		return new TraceSpanDto(
				getKey().getEntityKey().getTraceEntityId(),
				getKey().getThreadId(),
				getKey().getSequence(),
				getParentSequence(),
				getName(),
				getInfo(),
				getCreated(),
				getDuration(),
				getDurationNano());
	}

	@Override
	public Class<TraceSpanKey> getKeyClass(){
		return TraceSpanKey.class;
	}

	public TraceThreadKey getThreadKey(){
		return new TraceThreadKey(this.getTraceId(), this.getThreadId());
	}

}
