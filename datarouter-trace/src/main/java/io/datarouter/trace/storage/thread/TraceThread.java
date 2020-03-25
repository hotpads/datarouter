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
package io.datarouter.trace.storage.thread;

import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.trace.storage.entity.TraceEntityKey;
import io.datarouter.trace.storage.trace.Trace;

public class TraceThread extends BaseTraceThread<TraceEntityKey,TraceThreadKey,TraceThread>{

	public TraceThread(){
		super(new TraceThreadKey());
	}

	public TraceThread(String traceId, Long threadId){
		super(new TraceThreadKey(traceId, threadId));
	}

	public TraceThread(TraceThreadDto dto){
		super(new TraceThreadKey(dto.getTraceId(), dto.getThreadId()), dto);
	}

	public static class TraceThreadFielder extends BaseTraceThreadFielder<TraceEntityKey,TraceThreadKey,TraceThread>{

		public TraceThreadFielder(){
			super(TraceThreadKey.class);
			addOption(Trace.TTL_FIELDER_CONFIG);
		}

	}

	@Override
	public Class<TraceThreadKey> getKeyClass(){
		return TraceThreadKey.class;
	}

}
