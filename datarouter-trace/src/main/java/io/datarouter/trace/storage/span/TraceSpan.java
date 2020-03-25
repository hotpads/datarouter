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
package io.datarouter.trace.storage.span;

import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.trace.storage.entity.TraceEntityKey;
import io.datarouter.trace.storage.thread.TraceThreadKey;
import io.datarouter.trace.storage.trace.Trace;

public class TraceSpan extends BaseTraceSpan<TraceEntityKey,TraceSpanKey,TraceThreadKey,TraceSpan>{

	public TraceSpan(){
		super(new TraceSpanKey());
	}

	public TraceSpan(String traceId, Long threadId, Integer sequence, Integer parentSequence){
		super(new TraceSpanKey(traceId, threadId, sequence));
		this.parentSequence = parentSequence;
	}

	public TraceSpan(TraceSpanDto dto){
		super(new TraceSpanKey(dto.getTraceId(), dto.getThreadId(), dto.getSequence()), dto);
	}

	public static class TraceSpanFielder
	extends BaseTraceSpanFielder<TraceEntityKey,TraceSpanKey,TraceThreadKey,TraceSpan>{

		public TraceSpanFielder(){
			super(TraceSpanKey.class);
			addOption(Trace.TTL_FIELDER_CONFIG);
		}

	}

	@Override
	public Class<TraceSpanKey> getKeyClass(){
		return TraceSpanKey.class;
	}

	@Override
	public TraceThreadKey getThreadKey(){
		return new TraceThreadKey(getKey().getEntityKey().getTraceEntityId(), getKey().getThreadId());
	}

}
