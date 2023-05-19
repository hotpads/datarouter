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

import java.util.Optional;

public class Trace2SpanDto{

	public final Traceparent traceparent;
	public final Long parentThreadId;
	public final Integer sequence;
	public final Integer parentSequence;
	public final String name;
	public final TraceSpanGroupType groupType;
	public final Long created;

	private String info;
	private Long ended;
	private Long cpuTimeCreatedNs;
	private Long cpuTimeEndedNs;
	private Long memoryAllocatedBytesBegin;
	private Long memoryAllocatedBytesEnded;

	public Trace2SpanDto(
			Traceparent traceparent,
			Long parentThreadId,
			Integer sequence,
			Integer parentSequence,
			String name,
			TraceSpanGroupType groupType,
			Long created){
		this.traceparent = traceparent;
		this.parentThreadId = parentThreadId;
		this.sequence = sequence;
		this.parentSequence = parentSequence;
		this.name = name;
		this.groupType = groupType;
		this.created = created;
	}

	public Trace2SpanDto(
			Traceparent traceparent,
			Long parentThreadId,
			Integer sequence,
			Integer parentSequence,
			String name,
			TraceSpanGroupType groupType,
			String info,
			Long created,
			Long ended){
		this(traceparent, parentThreadId, sequence, parentSequence, name, groupType, created);
		this.info = info;
		this.ended = ended;
	}

	public Traceparent getTraceparent(){
		return traceparent;
	}

	public Long getParentThreadId(){
		return parentThreadId;
	}

	public Integer getSequence(){
		return sequence;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public Integer getParentSequenceOrMinusOne(){
		return Optional.ofNullable(parentSequence).orElse(-1);
	}

	public String getName(){
		return name;
	}

	public String getInfo(){
		return info;
	}

	public void setInfo(String info){
		this.info = info;
	}

	public Long getCreated(){
		return created;
	}

	public void setEnded(long ended){
		this.ended = ended;
	}

	public Long getEnded(){
		return ended;
	}

	public void markFinish(){
		this.ended = Trace2Dto.getCurrentTimeInNs();
	}

	public Long getCpuTimeCreatedNs(){
		return cpuTimeCreatedNs;
	}

	public void setCpuTimeCreated(Long cpuTimeCreatedNs){
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

	public void setMemoryAllocatedBegin(Long memoryAllocatedBytesBegin){
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
	}

	public Long getMemoryAllocatedBytesEnded(){
		return memoryAllocatedBytesEnded;
	}

	public void setMemoryAllocatedBytesEnded(Long memoryAllocatedBytesEnded){
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
	}

}
