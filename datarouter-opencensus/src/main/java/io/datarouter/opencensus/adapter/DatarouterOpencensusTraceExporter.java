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
package io.datarouter.opencensus.adapter;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TraceBundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.conveyor.TraceBuffers;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter.Handler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterOpencensusTraceExporter extends Handler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterOpencensusTraceExporter.class);

	@Inject
	private TraceBuffers traceBuffers;

	@Override
	public void export(Collection<SpanData> spanDataList){
		logger.info("DatarouterOpencensusTraceExporter starts exporting spanData from opencensus");
		Map<SpanId,SpanData> bindingSpans = Scanner.of(spanDataList)
				.include(spanData -> spanData.getAttributes().getAttributeMap()
						.containsKey(DatarouterOpencensusTool.TRACEPARENT_ATTRIBUTE_KEY))
				.toMap(spanData -> spanData.getContext().getSpanId());
		if(bindingSpans.isEmpty()){
			logger.info("no binding spans found.");
			return;
		}
		Map<SpanId,SpanId> parents = Scanner.of(spanDataList)
				.exclude(span -> span.getParentSpanId() == null)
				.toMap(span -> span.getContext().getSpanId(), SpanData::getParentSpanId);
		Map<Traceparent,Map<Long,Integer>> sequenceByThreadIdByTraceparent = new HashMap<>();
		Map<SpanId,Integer> sequenceBySpanId = new HashMap<>();
		Scanner.of(spanDataList)
				.exclude(spanData -> bindingSpans.containsKey(spanData.getContext().getSpanId()))
				.map(spanData -> {
					SpanData bindingSpan = bindingSpans.get(findBindingSpan(spanData.getContext().getSpanId(),
							parents));
					if(bindingSpan == null){
						logger.info("No binding span for: {}", spanData);
						return null;
					}
					Map<String,AttributeValue> attributes = bindingSpan.getAttributes().getAttributeMap();
					Traceparent traceparent = Traceparent.parseIfValid(getInnerValue(attributes.get(
							DatarouterOpencensusTool.TRACEPARENT_ATTRIBUTE_KEY))).get();
					Long threadId = getInnerValue(attributes.get(DatarouterOpencensusTool.THREAD_ID_ATTRIBUTE_KEY));
					Function<SpanId,Integer> nextSequenceGenerator = $ -> sequenceByThreadIdByTraceparent
							.computeIfAbsent(traceparent, $$ -> new HashMap<>())
							.compute(threadId, ($$, old) -> old == null ? 10000 : old + 1);
					Integer sequenceParent;
					if(bindingSpan.getContext().getSpanId().equals(spanData.getParentSpanId())){
						sequenceParent = getIntegerValue(attributes.get(
								DatarouterOpencensusTool.SEQUENCE_PARENT_ATTRIBUTE_KEY));
					}else{
						sequenceParent = sequenceBySpanId.computeIfAbsent(spanData.getParentSpanId(),
								nextSequenceGenerator);
					}
					Integer sequence = sequenceBySpanId.computeIfAbsent(spanData.getContext().getSpanId(),
							nextSequenceGenerator);
					return new TraceSpanDto(
							traceparent,
							threadId,
							sequence,
							sequenceParent,
							spanData.getName(),
							TraceSpanGroupType.DATABASE,
							null,
							toNanoTimestamp(toInstant(spanData.getStartTimestamp())),
							toNanoTimestamp(toInstant(spanData.getEndTimestamp())));
				})
				.include(Objects::nonNull)
				.groupBy(TraceSpanDto::getTraceparent)
				.values()
				.stream()
				.map(spans -> new TraceBundleDto(null, List.of(), spans))
				.map(bundleDto -> new TraceBundleAndHttpRequestRecordDto(bundleDto, null))
				.forEach(traceBuffers::offer);
	}

	private static int getIntegerValue(AttributeValue attributeValue){
		return DatarouterOpencensusTraceExporter.<Long>getInnerValue(attributeValue).intValue();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getInnerValue(AttributeValue attributeValue){
		return (T) attributeValue.match(x -> x, x -> x, x -> x, x -> x, x -> x);
	}

	private static Instant toInstant(Timestamp timestamp){
		return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
	}

	private static SpanId findBindingSpan(SpanId spanId, Map<SpanId,SpanId> parents){
		SpanId bindingSpan = spanId;
		while(parents.containsKey(bindingSpan)){
			bindingSpan = parents.get(bindingSpan);
		}
		return bindingSpan;
	}

	private static Long toNanoTimestamp(Instant instant){
		return instant.getEpochSecond() * 1_000_000_000 + instant.getNano();
	}

}
