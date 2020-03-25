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
package io.datarouter.trace.web;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.trace.storage.entity.BaseTraceEntity;
import io.datarouter.trace.storage.span.BaseTraceSpan;
import io.datarouter.trace.storage.thread.BaseTraceThread;
import io.datarouter.trace.storage.trace.BaseTrace;
import io.datarouter.util.UlidTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalString;

public abstract class BaseTraceHandler extends BaseHandler{

	protected Mav initMav(){
		return new Mav(getViewTraceJsp());
	}

	@Handler(defaultHandler = true)
	protected Mav viewTrace(OptionalString id){
		Mav mav = initMav();
		if(id.isEmpty()){
			return mav;
		}
		String traceId = id.get();
		traceId = trimId(traceId);
		BaseTraceEntity<?> traceEnity = getTrace(traceId);
		if(traceEnity == null){
			Duration duration = Duration.between(UlidTool.getInstant(traceId), Instant.now());
			String durationStr = new DatarouterDuration(duration.toNanos(), TimeUnit.NANOSECONDS).toString();
			return new MessageMav("Trace with id=" + traceId + " (" + durationStr + " old) not found");
		}
		mav.put("trace", toJspDto(traceEnity.getTrace()));
		Collection<TraceThreadDto> threads = IterableTool.map(traceEnity.getTraceThreads(), BaseTraceThread::toDto);
		if(threads.isEmpty()){
			return new MessageMav("no threads found (yet)");
		}
		Integer discardedThreadCount = getDiscardedThreadCountFromTrace(traceEnity.getTrace());
		Integer discardedSpanCount = getDiscardedSpanCountFromThreads(threads);
		TraceThreadGroup rootGroup = TraceThreadGroup.create(threads, makeFakeRootThread(traceEnity.getTrace()
				.toDto()));
		Collection<TraceSpanDto> spans = IterableTool.map(traceEnity.getTraceSpans(), BaseTraceSpan::toDto);
		rootGroup.setSpans(spans);
		mav.put("spans", spans);
		mav.put("numSpans", spans.size());
		mav.put("numDiscardedSpans", discardedSpanCount);
		mav.put("numDiscardedThreads", discardedThreadCount);
		mav.put("threadGroup", rootGroup);
		mav.put("threadGroupHtml", rootGroup.getHtml());
		return mav;
	}

	private String trimId(String traceId){
		if(StringTool.containsCaseInsensitive(traceId, "=")){
			return StringTool.getStringAfterLastOccurrence('=', traceId);
		}
		return traceId;
	}

	// fake root for corrupted entity
	private TraceThreadDto makeFakeRootThread(TraceDto trace){
		return new TraceThreadDto(trace.getTraceId(), 0L, null, "Fake root thread", null, null, trace.getCreated(), 0L,
				0L, 0, null);
	}

	private Integer getDiscardedSpanCountFromThreads(Collection<TraceThreadDto> threads){
		return threads.stream()
				.filter(thread -> thread.getDiscardedSpanCount() != null)
				.mapToInt(TraceThreadDto::getDiscardedSpanCount)
				.sum();
	}

	private Integer getDiscardedThreadCountFromTrace(BaseTrace<?,?,?> baseTrace){
		return baseTrace.getDiscardedThreadCount();
	}

	protected abstract PathNode getViewTraceJsp();
	protected abstract BaseTraceEntity<?> getTrace(String traceId);

	protected TraceJspDto toJspDto(BaseTrace<?,?,?> trace){
		return new TraceJspDto(
				trace.getTraceId(),
				trace.getType(),
				trace.getParams(),
				trace.getCreated(),
				trace.getDuration());
	}

	public static class TraceJspDto{

		private final String traceId;
		private final String type;
		private final String params;
		private final Long created;
		private final Long duration;

		public TraceJspDto(String traceId, String type, String params, Long created, Long duration){
			this.traceId = traceId;
			this.created = created;
			this.type = type;
			this.params = params;
			this.duration = duration;
		}

		public String getTraceId(){
			return traceId;
		}

		public Long getDuration(){
			return duration;
		}

		public Long getCreated(){
			return created;
		}

		public Date getTime(){
			return new Date(created);
		}

		public String getRequestString(){
			return type + Optional.ofNullable(params).map(params -> "?" + params).orElse("");
		}

	}

}
