package com.hotpads.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;


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
		if(getCurrentThread() == null) {
			return null;
		}
		return getCurrentThread().getId();
	}
	
	@Override
	public void createAndStartThread(String name){
		createThread(name);
		startThread();
	}
	
	@Override
	public void createThread(String name){
		Long traceId = getTraceId();
		if(traceId==null){
			return;
		}
		boolean hasParent = getTraceThreadParentId()!=null;
		TraceThread thread = new TraceThread(traceId, hasParent);
		thread.setParentId(getTraceThreadParentId());
		thread.setServerId(getServerName());
		thread.setName(name);
		setCurrentThread(thread);
	}
	
	@Override
	public void startThread(){
		if(getCurrentThread()==null){
			return;
		}
		getCurrentThread().markStart();
	}
	
	@Override
	public void appendToThreadName(String text){
		if(getCurrentThread()==null){
			return;
		}
		TraceThread thread = getCurrentThread();
		boolean addSpace = DrStringTool.notEmpty(thread.getName());
		thread.setName(DrStringTool.nullSafe(thread.getName()) + (addSpace ? " " : "") + text);
	}
	
	@Override
	public void appendToThreadInfo(String text){
		if(getCurrentThread()==null){
			return;
		}
		TraceThread thread = getCurrentThread();
		boolean addSpace = DrStringTool.notEmpty(thread.getInfo());
		thread.setInfo(DrStringTool.nullSafe(thread.getInfo()) + (addSpace ? " " : "") + text);
	}
	
	@Override
	public void finishThread(){
		if(getCurrentThread()==null){
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
		if(currentThread==null){
			return;
		}
		Integer parentSequence = null;
		if(DrCollectionTool.notEmpty(getSpanStack())){
			TraceSpan parent = getSpanStack().get(getSpanStack().size()-1);
			parentSequence = parent.getSequence();
		}
		TraceSpan span = new TraceSpan(
				currentThread.getTraceId(), 
				currentThread.getId(), 
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
		if(getCurrentSpan()==null){ 
			return;
		}
		TraceSpan span = getCurrentSpan();
		boolean addSpace = DrStringTool.notEmpty(span.getName());
		span.setName(DrStringTool.nullSafe(span.getName()) + (addSpace ? " " : "") + text);
		Counters.inc(span.getName());//yes, this is double-counting the span
	}
	
	@Override
	public void appendToSpanInfo(String text){
		if(getCurrentSpan()==null){
			return;
		}
		TraceSpan span = getCurrentSpan();
		boolean addSpace = DrStringTool.notEmpty(span.getInfo());
		span.setInfo(DrStringTool.nullSafe(span.getInfo()) + (addSpace ? " " : "") + text);
	}
	
	@Override
	public void finishSpan(){
		if(getCurrentSpan()==null){
			return;
		}
		getCurrentSpan().markFinish();
		getSpans().add(getCurrentSpan());
		popSpanFromStack();
	}
	
	
	/*************** private TraceSpan methods ***************************************/
	
	private TraceSpan getCurrentSpan(){
		if(DrCollectionTool.isEmpty(spanStack)) {
			return null;
		}
		return spanStack.get(spanStack.size() - 1);
	}
	
	private TraceSpan popSpanFromStack(){
		if(DrCollectionTool.isEmpty(spanStack)) {
			return null;
		}
		TraceSpan span = getCurrentSpan();
		spanStack.remove(spanStack.size() - 1);
		return span;
	}
	
	
	/****************** collectors ******************************/
	
	public <V> void collect(TracedCallable<V> callable){
		threads.addAll(DrCollectionTool.nullSafe(callable.getThreads()));
		spans.addAll(DrCollectionTool.nullSafe(callable.getSpans()));
	}
	
	
	/*********************** Object *********************************************/
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"["+currentThread.getName()+"]";
	}

	
	/*********************** get/set *********************************************/

	public TraceThread getCurrentThread(){
		return currentThread;
	}

	public void setCurrentThread(TraceThread currentThread){
		this.currentThread = currentThread;
	}

	public List<TraceThread> getThreads(){
		return threads;
	}

	public List<TraceSpan> getSpans(){
		return spans;
	}

	public String getServerName(){
		return serverName;
	}

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
