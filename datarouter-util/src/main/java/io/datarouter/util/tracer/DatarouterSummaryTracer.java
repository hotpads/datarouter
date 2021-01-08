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
package io.datarouter.util.tracer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import io.datarouter.instrumentation.trace.TraceContext;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;

public class DatarouterSummaryTracer implements Tracer{

	private static final Long MAX_SUMMARY_KEYS = 1_000L;

	public static final String INTERSPAN = "interspan";

	private final String traceId;
	private final String keyPrefix;

	private final Deque<SpanRecord> spans = new ConcurrentLinkedDeque<>();

	private final List<DatarouterSummaryTracer> childSummaryTracers = new ArrayList<>();
	private final Map<String,SpanSummary> summaryMap = new HashMap<>();

	private boolean truncated = false;

	public DatarouterSummaryTracer(){
		this(UlidTool.nextUlid(), null);
	}

	private DatarouterSummaryTracer(String traceId, String keyPrefix){
		this.traceId = traceId;
		this.keyPrefix = keyPrefix == null ? "" : (keyPrefix + " ");
	}

	/*---------------------------- Tracer------------------------------------*/

	@Override
	public Tracer createChildTracer(){
		var childTracer = new DatarouterSummaryTracer(traceId, getSpanRecordsPrefix());
		childSummaryTracers.add(childTracer);
		return childTracer;
	}

	/*---------------------------- toString ---------------------------------*/

	@Override
	public String toString(){
		return Scanner.of(childSummaryTracers)
				.map(DatarouterSummaryTracer::getSummaryMap)
				.append(List.of(summaryMap))
				.concatIter(Map::entrySet)
				.toMap(Entry::getKey, Entry::getValue, SpanSummary::merge)
				.entrySet().stream()
				.map(ent -> String.format("%s count=%s duration=%s", ent.getKey(), ent.getValue().count, ent.getValue()
						.duration))
				.collect(Collectors.joining(", ", "traceId=" + traceId + ", truncated=" + truncated + ", ", ""));
	}

	/*---------------------------- TraceThread ------------------------------*/

	@Override
	public void createThread(String name, long queueTimeMs){
		return;
	}

	@Override
	public void startThread(){
		return;
	}

	@Override
	public void finishThread(){
		return;
	}

	@Override
	public void addThread(TraceThreadDto thread){
		return;
	}

	@Override
	public void appendToThreadInfo(String text){
		return;
	}

	@Override
	public Integer getDiscardedThreadCount(){
		return 0;
	}

	@Override
	public void incrementDiscardedThreadCount(int discardedThreadCount){
		return;
	}

	@Override
	public BlockingQueue<TraceThreadDto> getThreadQueue(){
		return new ArrayBlockingQueue<>(1);//capacity must be >0
	}

	/*---------------------------- TraceSpan --------------------------------*/

	@Override
	public void startSpan(String name){
		SpanRecord prevRecord = spans.peek();
		if(prevRecord != null && prevRecord.name == INTERSPAN){
			addSummary(INTERSPAN, prevRecord.startMs, true);
			spans.pop();
		}
		spans.push(new SpanRecord(name, System.currentTimeMillis()));
	}

	@Override
	public void finishSpan(){
		String summaryKey = getSpanRecordsPrefix();
		SpanRecord currentSpan = spans.pop();
		addSummary(summaryKey, currentSpan.startMs, false);
		startSpan(INTERSPAN);
	}

	@Override
	public void addSpan(TraceSpanDto span){
		return;
	}

	@Override
	public void appendToSpanInfo(String text){
		return;
	}

	@Override
	public Integer getDiscardedSpanCount(){
		return 0;
	}

	@Override
	public void incrementDiscardedSpanCount(int discardedSpanCount){
		return;
	}

	@Override
	public BlockingQueue<TraceSpanDto> getSpanQueue(){
		return new ArrayBlockingQueue<>(1);//capacity must be >0
	}

	/*---------------------------- get/set ----------------------------------*/

	@Override
	public String getServerName(){
		return null;
	}

	@Override
	public String getTraceId(){
		return traceId;
	}

	@Override
	public Long getCurrentThreadId(){
		return null;
	}

	@Override
	public boolean getForceSave(){
		return false;
	}

	@Override
	public void setForceSave(){
		return;
	}

	public String getSpanRecordsPrefix(){
		return Scanner.of(spans.descendingIterator())
				.map(record -> record.name)
				.collect(Collectors.joining(" ", keyPrefix, ""));
	}

	public Map<String,SpanSummary> getSummaryMap(){
		return summaryMap;
	}

	private void addSummary(String key, Long spanStartMs, boolean skipIf0Duration){
		if(!truncated){
			long durationMs = System.currentTimeMillis() - spanStartMs;
			if(skipIf0Duration && durationMs == 0){
				return;
			}

			if(summaryMap.size() <= MAX_SUMMARY_KEYS){
				summaryMap.computeIfAbsent(key, $ -> new SpanSummary())
						.addSpan(durationMs);
			}else{
				truncated = true;
			}
		}
	}

	private static class SpanRecord{

		private final String name;
		private final Long startMs;

		private SpanRecord(String name, Long startMs){
			this.name = name;
			this.startMs = startMs;
		}

	}

	private static class SpanSummary{

		private Long count = 0L;
		private Long duration = 0L;

		private SpanSummary merge(SpanSummary other){
			this.count += other.count;
			this.duration += other.duration;
			return this;
		}

		private SpanSummary addSpan(Long duration){
			this.count++;
			this.duration += duration;
			return this;
		}

	}

	@Override
	public Optional<TraceContext> getTraceContext(){
		return Optional.empty();
	}

}
