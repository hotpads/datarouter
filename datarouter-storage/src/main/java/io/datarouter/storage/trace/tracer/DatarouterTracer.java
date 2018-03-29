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

import io.datarouter.storage.counter.Counters;
import io.datarouter.storage.trace.databean.TraceSpan;
import io.datarouter.storage.trace.databean.TraceThread;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.string.StringTool;

public class DatarouterTracer implements Tracer{

	private final String serverName;
	private final Long traceId;
	private final Long traceThreadParentId;

	private Integer nextSpanSequence = 0;

	private TraceThread currentThread;//should we be holding a map of current threads?  not sure yet
	private final List<TraceThread> threads = Collections.synchronizedList(new LinkedList<>());

	private final List<TraceSpan> spanStack = new ArrayList<>();
	private final List<TraceSpan> spans = Collections.synchronizedList(new LinkedList<>());


	/***************** constructors **********************************/

	public DatarouterTracer(String serverName, Long traceId, Long traceThreadParentId){
		this.serverName = serverName;
		this.traceId = traceId;
		this.traceThreadParentId = traceThreadParentId;
	}


	/*************** TraceThread methods *************************************/

	@Override
	public Long getCurrentThreadId(){
		if(getCurrentThread() == null){
			return null;
		}
		return getCurrentThread().getTraceId();
	}

	@Override
	public void createAndStartThread(String name){
		createThread(name);
		startThread();
	}

	@Override
	public void createThread(String name){
		Long traceId = getTraceId();
		if(traceId == null){
			return;
		}
		boolean hasParent = getTraceThreadParentId() != null;
		TraceThread thread = new TraceThread(traceId, hasParent);
		thread.setParentId(getTraceThreadParentId());
		thread.setServerId(getServerName());
		thread.setName(name);
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
	public void appendToThreadName(String text){
		if(getCurrentThread() == null){
			return;
		}
		TraceThread thread = getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getName());
		thread.setName(StringTool.nullSafe(thread.getName()) + (addSpace ? " " : "") + text);
	}

	@Override
	public void appendToThreadInfo(String text){
		if(getCurrentThread() == null){
			return;
		}
		TraceThread thread = getCurrentThread();
		boolean addSpace = StringTool.notEmpty(thread.getInfo());
		thread.setInfo(StringTool.nullSafe(thread.getInfo()) + (addSpace ? " " : "") + text);
	}

	@Override
	public void finishThread(){
		if(getCurrentThread() == null){
			return;
		}
		TraceThread thread = getCurrentThread();
		thread.markFinish();
		getThreads().add(thread);
		setCurrentThread(null);
	}


	/********************** TraceSpan methods **********************/

	@Override
	public void startSpan(String name){
		Counters.inc(name);
		if(currentThread == null){
			return;
		}
		Integer parentSequence = null;
		if(CollectionTool.notEmpty(getSpanStack())){
			TraceSpan parent = getSpanStack().get(getSpanStack().size() - 1);
			parentSequence = parent.getSequence();
		}
		TraceSpan span = new TraceSpan(
				currentThread.getTraceId(),
				currentThread.getTraceId(),
				nextSpanSequence,
				parentSequence);
		span.setName(name);
		getSpanStack().add(span);
		++nextSpanSequence;
	}

	/*
	 * Use this method carefully, because span names become Counter entries.  We do not want to
	 * create new counter entries for an unbounded set of names.  Use "appendToSpanInfo" to add
	 * things like the number of results returned from a method.
	 */
	@Override
	public void appendToSpanName(String text){
		if(getCurrentSpan() == null){
			return;
		}
		TraceSpan span = getCurrentSpan();
		boolean addSpace = StringTool.notEmpty(span.getName());
		span.setName(StringTool.nullSafe(span.getName()) + (addSpace ? " " : "") + text);
		Counters.inc(span.getName());//yes, this is double-counting the span
	}

	@Override
	public void appendToSpanInfo(String text){
		if(getCurrentSpan() == null){
			return;
		}
		TraceSpan span = getCurrentSpan();
		boolean addSpace = StringTool.notEmpty(span.getInfo());
		span.setInfo(StringTool.nullSafe(span.getInfo()) + (addSpace ? " " : "") + text);
	}

	@Override
	public void finishSpan(){
		if(getCurrentSpan() == null){
			return;
		}
		getCurrentSpan().markFinish();
		getSpans().add(getCurrentSpan());
		popSpanFromStack();
	}

	/*************** private TraceSpan methods ***************************************/

	private TraceSpan getCurrentSpan(){
		if(CollectionTool.isEmpty(spanStack)){
			return null;
		}
		return spanStack.get(spanStack.size() - 1);
	}

	private TraceSpan popSpanFromStack(){
		if(CollectionTool.isEmpty(spanStack)){
			return null;
		}
		TraceSpan span = getCurrentSpan();
		spanStack.remove(spanStack.size() - 1);
		return span;
	}

	/*********************** Object *********************************************/

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + currentThread.getName() + "]";
	}


	/*********************** get/set *********************************************/

	public TraceThread getCurrentThread(){
		return currentThread;
	}

	public void setCurrentThread(TraceThread currentThread){
		this.currentThread = currentThread;
	}

	@Override
	public List<TraceThread> getThreads(){
		return threads;
	}

	@Override
	public List<TraceSpan> getSpans(){
		return spans;
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	@Override
	public Long getTraceId(){
		return traceId;
	}

	public Long getTraceThreadParentId(){
		return traceThreadParentId;
	}

	public List<TraceSpan> getSpanStack(){
		return spanStack;
	}

}
