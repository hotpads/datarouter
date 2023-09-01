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
package io.datarouter.trace.storage.binarydto;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.trace.storage.binarydto.codec.TraceSpanGroupTypeFieldCodec;
import io.datarouter.trace.storage.binarydto.codec.TraceparentFieldCodec;

public class TraceSpanQueueBinaryDto extends BinaryDto<TraceSpanQueueBinaryDto>{

	@BinaryDtoField(index = 0, codec = TraceparentFieldCodec.class)
	public final Traceparent traceparent;
	@BinaryDtoField(index = 1)
	public final Long parentThreadId;
	@BinaryDtoField(index = 2)
	public final Integer sequence;
	@BinaryDtoField(index = 3)
	public final Integer parentSequence;
	@BinaryDtoField(index = 4)
	public final String name;
	@BinaryDtoField(index = 5, codec = TraceSpanGroupTypeFieldCodec.class)
	public final TraceSpanGroupType groupType;
	@BinaryDtoField(index = 6)
	public final Long created;
	@BinaryDtoField(index = 7)
	public final String info;
	@BinaryDtoField(index = 8)
	public final Long ended;
	@BinaryDtoField(index = 9)
	public final Long cpuTimeCreatedNs;
	@BinaryDtoField(index = 10)
	public final Long cpuTimeEndedNs;
	@BinaryDtoField(index = 11)
	public final Long memoryAllocatedBytesBegin;
	@BinaryDtoField(index = 12)
	public final Long memoryAllocatedBytesEnded;

	public TraceSpanQueueBinaryDto(
			Traceparent traceparent,
			Long parentThreadId,
			Integer sequence,
			Integer parentSequence,
			String name,
			TraceSpanGroupType groupType,
			Long created,
			String info,
			Long ended,
			Long cpuTimeCreatedNs,
			Long cpuTimeEndedNs,
			Long memoryAllocatedBytesBegin,
			Long memoryAllocatedBytesEnded){
		this.traceparent = traceparent;
		this.parentThreadId = parentThreadId;
		this.sequence = sequence;
		this.parentSequence = parentSequence;
		this.name = name;
		this.groupType = groupType;
		this.created = created;
		this.info = info;
		this.ended = ended;
		this.cpuTimeCreatedNs = cpuTimeCreatedNs;
		this.cpuTimeEndedNs = cpuTimeEndedNs;
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
	}

	public TraceSpanQueueBinaryDto(Trace2SpanDto dto){
		this(
				dto.traceparent,
				dto.parentThreadId,
				dto.sequence,
				dto.parentSequence,
				dto.name,
				dto.groupType,
				dto.created,
				dto.getInfo(),
				dto.getEnded(),
				dto.getCpuTimeCreatedNs(),
				dto.getCpuTimeEndedNs(),
				dto.getMemoryAllocatedBytesBegin(),
				dto.getMemoryAllocatedBytesEnded());
	}

	public Trace2SpanDto toTrace2SpanDto(){
		var dto = new Trace2SpanDto(
				traceparent,
				parentThreadId,
				sequence,
				parentSequence,
				name,
				groupType,
				info,
				created,
				ended);
		dto.setCpuTimeCreated(cpuTimeCreatedNs);
		dto.setCpuTimeEndedNs(cpuTimeEndedNs);
		dto.setMemoryAllocatedBegin(memoryAllocatedBytesBegin);
		dto.setMemoryAllocatedBytesEnded(memoryAllocatedBytesEnded);
		return dto;
	}

	public static TraceSpanQueueBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(TraceSpanQueueBinaryDto.class).decode(bytes);
	}

}