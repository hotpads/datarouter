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

public class Trace2ThreadDto{

	public final Traceparent traceparent;
	public final Long threadId;
	public final Long parentThreadId;
	public final String name;
	public final String info;
	public final String serverName;
	public final Long created;
	public final Long queuedDuration;
	public final Long runningDuration;
	public final Integer discardedSpanCount;
	public final String hostThreadName;
	public final Integer totalSpanCount;

	public Trace2ThreadDto(
			Traceparent traceparent,
			Long threadId,
			Long parentThreadId,
			String name,
			String info,
			String serverName,
			Long created,
			Long queuedDuration,
			Long runningDuration,
			Integer discardedSpanCount,
			String hostThreadName,
			Integer totalSpanCount){
		this.traceparent = traceparent;
		this.threadId = threadId;
		this.parentThreadId = parentThreadId;
		this.serverName = serverName;
		this.name = name;
		this.created = created;
		this.hostThreadName = hostThreadName;
		this.info = info;
		this.queuedDuration = queuedDuration;
		this.runningDuration = runningDuration;
		this.discardedSpanCount = discardedSpanCount;
		this.totalSpanCount = totalSpanCount;
	}

	public Long getTotalDuration(){
		long queued = queuedDuration == null ? 0 : queuedDuration;
		long running = runningDuration == null ? 0 : runningDuration;
		return queued + running;
	}

	public Traceparent getTraceparent(){
		return traceparent;
	}

	public Long getThreadId(){
		return threadId;
	}

	public Long getParentThreadId(){
		return parentThreadId;
	}

	public String getName(){
		return name;
	}

	public String getInfo(){
		return info;
	}

	public String getServerName(){
		return serverName;
	}

	public Long getCreated(){
		return created;
	}

	public Long getQueuedDuration(){
		return queuedDuration;
	}

	public Long getRunningDuration(){
		return runningDuration;
	}

	public Integer getDiscardedSpanCount(){
		return discardedSpanCount;
	}

	public String getHostThreadName(){
		return hostThreadName;
	}

	public Integer getTotalSpanCount(){
		return totalSpanCount;
	}

}
