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
package io.datarouter.trace;

import java.util.List;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.instrumentation.trace.TraceCategory;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceSaveReasonType;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.trace.storage.binarydto.TraceQueueBinaryDto;

public class TraceBinaryDtoTests{

	private static final TraceBundleDto VALID_BUNDLE_DTO = makeBundleDto();

	@Test
	public void testConstructorValidation(){
		Assert.assertThrows(NullPointerException.class, () -> new TraceQueueBinaryDto(null));
	}

	@Test
	public void testConstructorFields(){
		verifyOkDto(new TraceQueueBinaryDto(VALID_BUNDLE_DTO));
	}

	@Test
	public void testRoundTripBytes(){
		var bytes = new TraceQueueBinaryDto(VALID_BUNDLE_DTO).encodeIndexed();
		var okDto = TraceQueueBinaryDto.decode(new TraceQueueBinaryDto(VALID_BUNDLE_DTO).encodeIndexed());
		verifyOkDto(okDto);
		Assert.assertEquals(okDto.encodeIndexed(), bytes);
	}

	private void verifyOkDto(TraceQueueBinaryDto okDto){
		Assert.assertTrue(TraceBinaryDtoTests.equals(VALID_BUNDLE_DTO, okDto.toTraceBundleDto()));
	}

	private static TraceBundleDto makeBundleDto(){
		var traceparent = Traceparent.generateNewWithCurrentTimeInNs();
		var trace = new TraceDto(
				traceparent,
				"parent",
				"context",
				"type",
				"params",
				1L,
				2L,
				"service",
				3,
				4,
				5L,
				6L,
				7L,
				8L,
				List.of(TraceSaveReasonType.CPU, TraceSaveReasonType.QUERY_PARAM),
				TraceCategory.HTTP_REQUEST,
				"test");
		var thread = new TraceThreadDto(traceparent, 9L, 10L, "thread", "server", "host", 11L);
		thread.setInfo("info1");
		thread.setQueuedEnded(12L);
		thread.setEnded(13L);
		thread.setDiscardedSpanCount(14);
		thread.setTotalSpanCount(15);
		thread.setCpuTimeCreatedNs(16L);
		thread.setCpuTimeEndedNs(17L);
		thread.setMemoryAllocatedBytesBegin(18L);
		thread.setMemoryAllocatedBytesEnded(19L);
		var span = new TraceSpanDto(
				traceparent, 20L, 21, 22, "span", TraceSpanGroupType.CLOUD_STORAGE, "info2", 23L, 24L);
		span.setCpuTimeCreated(25L);
		span.setCpuTimeEndedNs(26L);
		span.setMemoryAllocatedBegin(27L);
		span.setMemoryAllocatedBytesEnded(28L);
		return new TraceBundleDto(trace, List.of(thread), List.of(span));
	}

	//only works for one item in threads/spans
	public static boolean equals(TraceBundleDto first, TraceBundleDto second){
		if(first == second){
			return true;
		}
		if(first == null || second == null){
			return false;
		}
		boolean dto = Objects.equals(first.traceDto.context, second.traceDto.context)
				&& Objects.equals(first.traceDto.cpuTimeCreatedNs, second.traceDto.cpuTimeCreatedNs)
				&& Objects.equals(first.traceDto.cpuTimeEndedNs, second.traceDto.cpuTimeEndedNs)
				&& Objects.equals(first.traceDto.created, second.traceDto.created)
				&& Objects.equals(first.traceDto.discardedThreadCount, second.traceDto.discardedThreadCount)
				&& Objects.equals(first.traceDto.ended, second.traceDto.ended)
				&& Objects.equals(first.traceDto.initialParentId, second.traceDto.initialParentId)
				&& Objects.equals(first.traceDto.memoryAllocatedBytesBegin, second.traceDto.memoryAllocatedBytesBegin)
				&& Objects.equals(first.traceDto.memoryAllocatedBytesEnded, second.traceDto.memoryAllocatedBytesEnded)
				&& Objects.equals(first.traceDto.params, second.traceDto.params)
				&& Objects.equals(first.traceDto.saveReasons, second.traceDto.saveReasons)
				&& Objects.equals(first.traceDto.serviceName, second.traceDto.serviceName)
				&& Objects.equals(first.traceDto.totalThreadCount,second.traceDto.totalThreadCount)
				&& Objects.equals(first.traceDto.traceparent, second.traceDto.traceparent)
				&& Objects.equals(first.traceDto.type, second.traceDto.type);
		var firstThread = first.traceThreadDtos.iterator().next();
		var secondThread = second.traceThreadDtos.iterator().next();
		boolean thread = Objects.equals(firstThread.getCpuTimeCreatedNs(), secondThread.getCpuTimeCreatedNs())
				&& Objects.equals(firstThread.getCpuTimeEndedNs(), secondThread.getCpuTimeEndedNs())
				&& Objects.equals(firstThread.created, secondThread.created)
				&& Objects.equals(firstThread.getDiscardedSpanCount(), secondThread.getDiscardedSpanCount())
				&& Objects.equals(firstThread.getEnded(), secondThread.getEnded())
				&& Objects.equals(firstThread.hostThreadName, secondThread.hostThreadName)
				&& Objects.equals(firstThread.getInfo(), secondThread.getInfo())
				&& Objects.equals(
						firstThread.getMemoryAllocatedBytesBegin(), secondThread.getMemoryAllocatedBytesBegin())
				&& Objects.equals(
						firstThread.getMemoryAllocatedBytesEnded(), secondThread.getMemoryAllocatedBytesEnded())
				&& Objects.equals(firstThread.name, secondThread.name)
				&& Objects.equals(firstThread.parentThreadId, secondThread.parentThreadId)
				&& Objects.equals(firstThread.getQueuedEnded(), secondThread.getQueuedEnded())
				&& Objects.equals(firstThread.serverName, secondThread.serverName)
				&& Objects.equals(firstThread.threadId, secondThread.threadId)
				&& Objects.equals(firstThread.getTotalSpanCount(), secondThread.getTotalSpanCount())
				&& Objects.equals(firstThread.traceparent, secondThread.traceparent);
		var firstSpan = first.traceSpanDtos.iterator().next();
		var secondSpan = second.traceSpanDtos.iterator().next();
		boolean span = Objects.equals(firstSpan.getCpuTimeCreatedNs(), secondSpan.getCpuTimeCreatedNs())
				&& Objects.equals(firstSpan.getCpuTimeEndedNs(), secondSpan.getCpuTimeEndedNs())
				&& Objects.equals(firstSpan.created, secondSpan.created)
				&& Objects.equals(firstSpan.getEnded(), secondSpan.getEnded())
				&& firstSpan.groupType == secondSpan.groupType
				&& Objects.equals(firstSpan.getInfo(), secondSpan.getInfo())
				&& Objects.equals(firstSpan.getMemoryAllocatedBytesBegin(), secondSpan.getMemoryAllocatedBytesBegin())
				&& Objects.equals(firstSpan.getMemoryAllocatedBytesEnded(), secondSpan.getMemoryAllocatedBytesEnded())
				&& Objects.equals(firstSpan.name, secondSpan.name)
				&& Objects.equals(firstSpan.parentSequence, secondSpan.parentSequence)
				&& Objects.equals(firstSpan.parentThreadId, secondSpan.parentThreadId)
				&& Objects.equals(firstSpan.sequence, secondSpan.sequence)
				&& Objects.equals(firstSpan.traceparent, secondSpan.traceparent);
		return dto && thread && span;
	}

}
