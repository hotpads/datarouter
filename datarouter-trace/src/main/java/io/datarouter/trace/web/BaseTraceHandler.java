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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.entity.UiTraceBundleDto;
import io.datarouter.trace.storage.span.BaseTraceSpan;
import io.datarouter.trace.storage.thread.BaseTraceThread;
import io.datarouter.trace.storage.trace.BaseTrace;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalString;

public abstract class BaseTraceHandler extends BaseHandler{

	protected Mav initMav(String traceId){
		return new Mav(getViewTraceJsp());
	}

	@Handler(defaultHandler = true)
	protected Mav viewTrace(OptionalString id){
		Optional<String> traceId = id.map(this::trimId);
		Mav mav = initMav(traceId.orElse(null));
		if(traceId.isEmpty()){
			return mav;
		}

		UiTraceBundleDto traceEnity;
		try{
			traceEnity = getTrace(traceId.get());
		}catch(AccessException e){
			mav.put("errorMessage", e.getMessage());
			return mav;
		}
		mav.put("trace", toJspDto(traceEnity.trace));
		List<TraceThreadDto> threads = Scanner.of(traceEnity.threads).map(BaseTraceThread::toDto).list();
		if(threads.isEmpty()){
			return new MessageMav("no threads found (yet)");
		}
		Integer discardedThreadCount = traceEnity.trace.getDiscardedThreadCount();
		Integer discardedSpanCount = getDiscardedSpanCountFromThreads(threads);
		TraceThreadGroup rootGroup = TraceThreadGroup.create(threads, makeFakeRootThread(traceEnity.trace.toDto()));
		Collection<TraceSpanDto> spans = Scanner.of(traceEnity.spans).map(BaseTraceSpan::toDto).list();
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
		return new TraceThreadDto(trace.getTraceId(), 0L, null, "Fake root thread", null, null, trace.getCreatedMs(),
				0L, 0L, 0, null);
	}

	private Integer getDiscardedSpanCountFromThreads(Collection<TraceThreadDto> threads){
		return threads.stream()
				.filter(thread -> thread.getDiscardedSpanCount() != null)
				.mapToInt(TraceThreadDto::getDiscardedSpanCount)
				.sum();
	}

	protected abstract PathNode getViewTraceJsp();
	protected abstract UiTraceBundleDto getTrace(String traceId) throws AccessException;

	protected TraceJspDto toJspDto(BaseTrace<?,?,?> trace){
		return new TraceJspDto(
				trace.getType(),
				trace.getParams(),
				trace.getCreated(),
				trace.getDuration());
	}

	public static class TraceJspDto{

		private final String type;
		private final String params;
		private final Long created;
		private final Long duration;

		public TraceJspDto(String type, String params, Long created, Long duration){
			this.created = created;
			this.type = type;
			this.params = params;
			this.duration = duration;
		}

		public Long getDurationMs(){
			return Trace2Dto.convertToMsFromNsIfNecessary(duration, created);
		}

		public Long getCreatedMs(){
			return Trace2Dto.convertToMsFromNsIfNecessary(created, created);
		}

		public Date getTime(){
			return new Date(created);
		}

		public String getRequestString(){
			return type + Optional.ofNullable(params).map(params -> "?" + params).orElse("");
		}

	}

}
