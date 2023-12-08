/*
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

	public final Traceparent traceparent;
	public final Long threadId;
	public final Long parentThreadId;
	public final String name;
	public final String serverName;
	public final String hostThreadName;
	public final Long created;
	private String info;
	private Long queuedEnded;
	private Long ended;
	private Integer discardedSpanCount;
	private Integer totalSpanCount;
	private Long cpuTimeCreatedNs;
	private Long cpuTimeEndedNs;
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesEnded;

	public TraceThreadDto(
			Traceparent traceparent,
			Long threadId,
			Long parentThreadId,
			String name,
			String serverName,
			String hostThreadName,
			Long created){
		this.traceparent = traceparent;
		this.threadId = threadId;
		this.parentThreadId = parentThreadId;
		this.name = name;
		this.serverName = serverName;
		this.hostThreadName = hostThreadName;
		this.created = created;
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

	public String getHostThreadName(){
		return hostThreadName;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

	public Long getQueuedEnded(){
		return queuedEnded;
	}

	public void setQueuedEnded(Long queuedEnded){
		this.queuedEnded = queuedEnded;
	}

	public void markStart(){
		this.queuedEnded = TraceTimeTool.epochNano();
	}

	public Long getEnded(){
		return ended;
	}

	public void setEnded(Long ended){
		this.ended = ended;
	}

	public String getServerName(){
		return serverName;
	}

	public Long getCreated(){
		return created;
	}

	public Integer getDiscardedSpanCount(){
		return discardedSpanCount;
	}

	public void setDiscardedSpanCount(Integer discardedSpanCount){
		this.discardedSpanCount = discardedSpanCount;
	}

	public Integer getTotalSpanCount(){
		return totalSpanCount;
	}

	public void setTotalSpanCount(Integer totalSpanCount){
		this.totalSpanCount = totalSpanCount;
	}

	public Long getCpuTimeCreatedNs(){
		return cpuTimeCreatedNs;
	}

	public void setCpuTimeCreatedNs(Long cpuTimeCreatedNs){
		this.cpuTimeCreatedNs = cpuTimeCreatedNs;
	}

	public Long getCpuTimeEndedNs(){
		return cpuTimeEndedNs;
	}

	public void setCpuTimeEndedNs(Long cpuTimeEndedNs){
		this.cpuTimeEndedNs = cpuTimeEndedNs;
	}

	public Long getMemoryAllocatedBytesBegin(){
		return memoryAllocatedBytesBegin;
	}

	public void setMemoryAllocatedBytesBegin(Long memoryAllocatedBytesBegin){
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
	}

	public Long getMemoryAllocatedBytesEnded(){
		return memoryAllocatedBytesEnded;
	}

	public void setMemoryAllocatedBytesEnded(Long memoryAllocatedBytesEnded){
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
	}

}
