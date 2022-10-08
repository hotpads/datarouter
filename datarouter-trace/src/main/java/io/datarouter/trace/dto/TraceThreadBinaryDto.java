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
package io.datarouter.trace.dto;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.Traceparent;

public class TraceThreadBinaryDto extends BinaryDto<TraceThreadBinaryDto>{

	@BinaryDtoField(index = 0, codec = TraceparentFieldCodec.class)
	public final Traceparent traceparent;
	@BinaryDtoField(index = 1)
	public final Long threadId;
	@BinaryDtoField(index = 2)
	public final Long parentThreadId;
	@BinaryDtoField(index = 3)
	public final String name;
	@BinaryDtoField(index = 4)
	public final String serverName;
	@BinaryDtoField(index = 5)
	public final String hostThreadName;
	@BinaryDtoField(index = 6)
	public final Long created;
	@BinaryDtoField(index = 7)
	public final String info;
	@BinaryDtoField(index = 8)
	public final Long queuedEnded;
	@BinaryDtoField(index = 9)
	public final Long ended;
	@BinaryDtoField(index = 10)
	public final Integer discardedSpanCount;
	@BinaryDtoField(index = 11)
	public final Integer totalSpanCount;
	@BinaryDtoField(index = 12)
	public final Long cpuTimeCreatedNs;
	@BinaryDtoField(index = 13)
	public final Long cpuTimeEndedNs;
	@BinaryDtoField(index = 14)
	public final Long memoryAllocatedBytesBegin;
	@BinaryDtoField(index = 15)
	public final Long memoryAllocatedBytesEnded;

	public TraceThreadBinaryDto(
			Traceparent traceparent,
			Long threadId,
			Long parentThreadId,
			String name,
			String serverName,
			String hostThreadName,
			Long created,
			String info,
			Long queuedEnded,
			Long ended,
			Integer discardedSpanCount,
			Integer totalSpanCount,
			Long cpuTimeCreatedNs,
			Long cpuTimeEndedNs,
			Long memoryAllocatedBytesBegin,
			Long memoryAllocatedBytesEnded){
		this.traceparent = traceparent;
		this.threadId = threadId;
		this.parentThreadId = parentThreadId;
		this.name = name;
		this.serverName = serverName;
		this.hostThreadName = hostThreadName;
		this.created = created;
		this.info = info;
		this.queuedEnded = queuedEnded;
		this.ended = ended;
		this.discardedSpanCount = discardedSpanCount;
		this.totalSpanCount = totalSpanCount;
		this.cpuTimeCreatedNs = cpuTimeCreatedNs;
		this.cpuTimeEndedNs = cpuTimeEndedNs;
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
	}

	public TraceThreadBinaryDto(Trace2ThreadDto dto){
		this(
				dto.traceparent,
				dto.threadId,
				dto.parentThreadId,
				dto.name,
				dto.serverName,
				dto.hostThreadName,
				dto.created,
				dto.getInfo(),
				dto.getQueuedEnded(),
				dto.getEnded(),
				dto.getDiscardedSpanCount(),
				dto.getTotalSpanCount(),
				dto.getCpuTimeCreatedNs(),
				dto.getCpuTimeEndedNs(),
				dto.getMemoryAllocatedBytesBegin(),
				dto.getMemoryAllocatedBytesEnded());
	}

	public Trace2ThreadDto toTrace2ThreadDto(){
		var dto = new Trace2ThreadDto(
				traceparent,
				threadId,
				parentThreadId,
				name,
				serverName,
				hostThreadName,
				created);
		dto.setInfo(info);
		dto.setQueuedEnded(queuedEnded);
		dto.setEnded(ended);
		dto.setDiscardedSpanCount(discardedSpanCount);
		dto.setTotalSpanCount(totalSpanCount);
		dto.setCpuTimeCreatedNs(cpuTimeCreatedNs);
		dto.setCpuTimeEndedNs(cpuTimeEndedNs);
		dto.setMemoryAllocatedBytesBegin(memoryAllocatedBytesBegin);
		dto.setMemoryAllocatedBytesEnded(memoryAllocatedBytesEnded);
		return dto;
	}

	public static TraceThreadBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(TraceThreadBinaryDto.class).decode(bytes);
	}

}