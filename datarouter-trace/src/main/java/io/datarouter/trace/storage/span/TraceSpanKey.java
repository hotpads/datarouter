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
/**
 *
 */
package io.datarouter.trace.storage.span;

import io.datarouter.trace.storage.entity.TraceEntityKey;

public class TraceSpanKey extends BaseTraceSpanKey<TraceEntityKey,TraceSpanKey>{

	public TraceSpanKey(){
		this.entityKey = new TraceEntityKey();
	}

	public TraceSpanKey(TraceEntityKey entityKey){
		this.entityKey = entityKey;
	}

	public TraceSpanKey(String traceId, Long threadId, Integer sequence){
		super(threadId, sequence);
		this.entityKey = new TraceEntityKey(traceId);
	}

	@Override
	public TraceSpanKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceSpanKey(entityKey);
	}

}