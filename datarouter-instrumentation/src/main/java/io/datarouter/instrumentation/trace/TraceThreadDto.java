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
package io.datarouter.instrumentation.trace;

public class TraceThreadDto{

	public final Long traceId;
	public final Long threadId;
	public final Long parentId;
	public final String name;
	public final String info;
	public final String serverId;
	public final Long created;
	public final Long queuedDuration;
	public final Long runningDuration;
	public final Long queuedDurationNano;
	public final Long runningDurationNano;

	public TraceThreadDto(Long traceId, Long threadId, Long parentId, String name, String info, String serverId,
			Long created, Long queuedDuration, Long runningDuration, Long queuedDurationNano, Long runningDurationNano){
		this.traceId = traceId;
		this.threadId = threadId;
		this.parentId = parentId;
		this.name = name;
		this.info = info;
		this.serverId = serverId;
		this.created = created;
		this.queuedDuration = queuedDuration;
		this.runningDuration = runningDuration;
		this.queuedDurationNano = queuedDurationNano;
		this.runningDurationNano = runningDurationNano;
	}

}
