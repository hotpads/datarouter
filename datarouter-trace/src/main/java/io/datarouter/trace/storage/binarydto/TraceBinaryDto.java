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

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.TraceSaveReasonType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.binarydto.codec.TraceSaveReasonTypeFieldCodec;
import io.datarouter.trace.storage.binarydto.codec.TraceparentFieldCodec;

public class TraceBinaryDto extends BinaryDto<TraceBinaryDto>{

	@BinaryDtoField(index = 0, codec = TraceparentFieldCodec.class)
	public final Traceparent traceparent;
	@BinaryDtoField(index = 1)
	public final String initialParentId;
	@BinaryDtoField(index = 2)
	public final String context;
	@BinaryDtoField(index = 3)
	public final String type;
	@BinaryDtoField(index = 4)
	public final String params;
	@BinaryDtoField(index = 5)
	public final Long created;
	@BinaryDtoField(index = 6)
	public final Long ended;
	@BinaryDtoField(index = 7)
	public final String serviceName;
	@BinaryDtoField(index = 8)
	public final Integer discardedThreadCount;
	@BinaryDtoField(index = 9)
	public final Integer totalThreadCount;
	@BinaryDtoField(index = 10)
	public final Long cpuTimeCreatedNs;
	@BinaryDtoField(index = 11)
	public final Long cpuTimeEndedNs;
	@BinaryDtoField(index = 12)
	public final Long memoryAllocatedBytesBegin;
	@BinaryDtoField(index = 13)
	public final Long memoryAllocatedBytesEnded;
	@BinaryDtoField(index = 14, codec = TraceSaveReasonTypeFieldCodec.class)
	public final List<TraceSaveReasonType> saveReasons;

	@BinaryDtoField(index = 15)
	public final List<TraceThreadBinaryDto> threads;
	@BinaryDtoField(index = 16)
	public final List<TraceSpanBinaryDto> spans;

	public TraceBinaryDto(Trace2BundleDto dto){
		this(
				dto.traceDto.traceparent,
				dto.traceDto.initialParentId,
				dto.traceDto.context,
				dto.traceDto.type,
				dto.traceDto.params,
				dto.traceDto.created,
				dto.traceDto.ended,
				dto.traceDto.serviceName,
				dto.traceDto.discardedThreadCount,
				dto.traceDto.totalThreadCount,
				dto.traceDto.cpuTimeCreatedNs,
				dto.traceDto.cpuTimeEndedNs,
				dto.traceDto.memoryAllocatedBytesBegin,
				dto.traceDto.memoryAllocatedBytesEnded,
				dto.traceDto.saveReasons,
				dto.traceThreadDtos == null
						? null
						: Scanner.of(dto.traceThreadDtos)
								.map(TraceThreadBinaryDto::new)
								.list(),
				dto.traceSpanDtos == null
						? null
						: Scanner.of(dto.traceSpanDtos)
								.map(TraceSpanBinaryDto::new)
								.list());
	}

	public TraceBinaryDto(
			Traceparent traceparent,
			String initialParentId,
			String context,
			String type,
			String params,
			Long created,
			Long ended,
			String serviceName,
			Integer discardedThreadCount,
			Integer totalThreadCount,
			Long cpuTimeCreatedNs,
			Long cpuTimeEndedNs,
			Long memoryAllocatedBytesBegin,
			Long memoryAllocatedBytesEnded,
			List<TraceSaveReasonType> saveReasons,
			List<TraceThreadBinaryDto> threads,
			List<TraceSpanBinaryDto> spans){
		this.traceparent = traceparent;
		this.initialParentId = initialParentId;
		this.context = context;
		this.type = type;
		this.params = params;
		this.created = created;
		this.ended = ended;
		this.serviceName = serviceName;
		this.discardedThreadCount = discardedThreadCount;
		this.totalThreadCount = totalThreadCount;
		this.cpuTimeCreatedNs = cpuTimeCreatedNs;
		this.cpuTimeEndedNs = cpuTimeEndedNs;
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
		this.saveReasons = saveReasons;

		this.threads = threads;
		this.spans = spans;
	}

	public Trace2BundleDto toTrace2BundleDto(){
		var traceDto = new Trace2Dto(
				traceparent,
				initialParentId,
				context,
				type,
				params,
				created,
				ended,
				serviceName,
				discardedThreadCount,
				totalThreadCount,
				cpuTimeCreatedNs,
				cpuTimeEndedNs,
				memoryAllocatedBytesBegin,
				memoryAllocatedBytesEnded,
				saveReasons);
		List<Trace2ThreadDto> threadDtos = null;
		if(threads != null){
			threadDtos = Scanner.of(threads)
					.map(TraceThreadBinaryDto::toTrace2ThreadDto)
					.list();
		}
		List<Trace2SpanDto> spanDtos = null;
		if(spans != null){
			spanDtos = Scanner.of(spans)
					.map(TraceSpanBinaryDto::toTrace2SpanDto)
					.list();
		}
		return new Trace2BundleDto(traceDto, threadDtos, spanDtos);
	}

	public static TraceBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(TraceBinaryDto.class).decode(bytes);
	}

}
