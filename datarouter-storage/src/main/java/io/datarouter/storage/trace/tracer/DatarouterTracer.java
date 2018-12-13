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
package io.datarouter.storage.trace.tracer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.string.StringTool;

public class DatarouterTracer implements Tracer{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterTracer.class);

	private static final int MAX_SPANS = 100;
	private static final int MAX_THREADS = 100;

	private final String serverName;
	private final String traceId;
	private final Long traceThreadParentId;

	private Integer nextSpanSequence = 0;

	private TraceThreadDto currentThread;//should we be holding a map of current threads?  not sure yet
	private final List<TraceThreadDto> threads = Collections.synchronizedList(new LinkedList<>());

	private final List<TraceSpanDto> spanStack = new ArrayList<>();
	private final List<TraceSpanDto> spans = Collections.synchronizedList(new LinkedList<>());

	public DatarouterTracer(String serverName, String traceId, Long traceThreadParentId){
		this.serverName = serverName;
		this.traceId = traceId;
		this.traceThreadParentId = traceThreadParentId;
	}

	/*---------------------------- TraceThread ------------------------------*/

	@Override
	public Long getCurrentThreadId(){
		if(getCurrentThread() == null){
			return null;
		}
		return getCurrentThread().getThreadId();
	}

	@Override
	public void createAndStartThread(String name){
		createThread(name);
		startThread();
	}

	@Override
	public void createThread(String name){
		String traceId = getTraceId();
		if(traceId == null){
			return;
		}
		Long parentId = getTraceThreadParentId();
		Long threadId = parentId == null ? 0L : RandomTool.nextPositiveLong();
		TraceThreadDto thread = new TraceThreadDto(
				traceId,
				threadId,
				parentId,
				getServerName(),
				name,
				System.currentTimeMillis());
		setCurrentThread(thread);
	}

	@Override
	public void startThread(){
		if(getCurrentThread() == null){
			return;
		}
		getCurrentThread().markStart();
	}

	@Override
	public void appendToThreadInfo(String text){
		if(getCurrentThread() == null){
			return;
		}
		TraceThreadDto thread = getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getInfo());
		thread.setInfo(StringTool.nullSafe(thread.getInfo()) + (addSpace ? " " : "") + text);
	}

	@Override
	public void finishThread(){
		if(getCurrentThread() == null){
			return;
		}
		TraceThreadDto thread = getCurrentThread();
		thread.markFinish();
		if(getThreads().size() < MAX_THREADS){
			getThreads().add(thread);
			setCurrentThread(null);
		}else{
			logger.debug("cannot add thread, max capacity reached traceId={}", traceId);
		}
	}


	/*---------------------------- TraceSpan --------------------------------*/

	@Override
	public void startSpan(String name){
		if(currentThread == null){
			return;
		}
		Integer parentSequence = null;
		if(CollectionTool.notEmpty(getSpanStack())){
			TraceSpanDto parent = getSpanStack().get(getSpanStack().size() - 1);
			parentSequence = parent.getSequence();
		}
		TraceSpanDto span = new TraceSpanDto(
				currentThread.getTraceId(),
				currentThread.getThreadId(),
				nextSpanSequence,
				parentSequence,
				System.currentTimeMillis());
		span.setName(name);
		getSpanStack().add(span);
		++nextSpanSequence;
	}

	@Override
	public void appendToSpanInfo(String text){
		if(getCurrentSpan() == null){
			return;
		}
		TraceSpanDto span = getCurrentSpan();
		boolean addSpace = StringTool.notEmpty(span.getInfo());
		span.setInfo(StringTool.nullSafe(span.getInfo()) + (addSpace ? " " : "") + text);
	}

	@Override
	public void finishSpan(){
		if(getCurrentSpan() == null){
			return;
		}
		getCurrentSpan().markFinish();
		if(getSpans().size() < MAX_SPANS){
			getSpans().add(getCurrentSpan());
			popSpanFromStack();
		}else{
			logger.debug("cannot add span, max capacity traceId={}", traceId);
		}
	}

	/*---------------------------- private TraceSpan ------------------------*/

	private TraceSpanDto getCurrentSpan(){
		if(CollectionTool.isEmpty(spanStack)){
			return null;
		}
		return spanStack.get(spanStack.size() - 1);
	}

	private TraceSpanDto popSpanFromStack(){
		if(CollectionTool.isEmpty(spanStack)){
			return null;
		}
		TraceSpanDto span = getCurrentSpan();
		spanStack.remove(spanStack.size() - 1);
		return span;
	}

	/*---------------------------- object -----------------------------------*/

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + currentThread.getName() + "]";
	}

	/*---------------------------- get/set ----------------------------------*/

	public TraceThreadDto getCurrentThread(){
		return currentThread;
	}

	public void setCurrentThread(TraceThreadDto currentThread){
		this.currentThread = currentThread;
	}

	@Override
	public List<TraceThreadDto> getThreads(){
		return threads;
	}

	@Override
	public List<TraceSpanDto> getSpans(){
		return spans;
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	@Override
	public String getTraceId(){
		return traceId;
	}

	public Long getTraceThreadParentId(){
		return traceThreadParentId;
	}

	public List<TraceSpanDto> getSpanStack(){
		return spanStack;
	}

}
