package com.hotpads.trace;

import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;


public class TraceContext {
	protected static Logger logger = Logger.getLogger(TraceContext.class);
	
	protected String serverId;
	protected Long traceId;
	protected Long traceThreadParentId;
	protected Integer nextSpanSequence = 0;
	
	protected TraceThread currentThread;//should we be holding a map of current threads?  not sure yet
	protected List<TraceThread> threads = ListTool.createLinkedList();
	
	protected TraceSpan currentSpan;
	protected List<TraceSpan> spans = ListTool.createLinkedList();
	
	
	/***************** constructors **********************************/
	
	public TraceContext(String serverId, Long traceId, Long traceThreadParentId){
		this.serverId = serverId;
		this.traceId = traceId;
		this.traceThreadParentId = traceThreadParentId;
	}

	
	/***************** static Trace methods ********************************************/
	
	
	
	/*************** static TraceThread methods *************************************/
	
	public Long getCurrentThreadId(){
		TraceContext ctx = get();
		if(ctx==null){ return null; }
		if(ctx.getCurrentThread()==null){ return null; }
		return ctx.getCurrentThread().getId();
	}
	
	public static void createAndStartThread(String name, Long parentId){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		createThread(name);
		startThread();
	}
	
	public static void createThread(String name){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		Long traceId = ctx.getTraceId();
		if(traceId==null){ return; }
		boolean hasParent = ctx.getTraceThreadParentId()!=null;
		TraceThread thread = new TraceThread(traceId, hasParent);
		thread.setParentId(ctx.getTraceThreadParentId());
		thread.setServerId(ctx.getServerId());
		thread.setName(name);
		ctx.setCurrentThread(thread);
	}
	
	public static void startThread(){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		if(ctx.getCurrentThread()==null){ return; }
		ctx.getCurrentThread().markStart();
	}
	
	public static void finishThread(){
		TraceContext ctx = get();
		if(ctx==null){ return; }
		if(ctx.getCurrentThread()==null){ return; }
		TraceThread thread = ctx.getCurrentThread();
		thread.markFinish();
		ctx.getThreads().add(thread);
		ctx.setCurrentThread(null);
	}
	
	/*************** static TraceSpan methods *************************************/
		
	public static void startSpan(String name){
		TraceContext ctx = get();
		if(ctx==null || ctx.currentThread==null){ return; }
		TraceSpan span = new TraceSpan(
				ctx.currentThread.getTraceId(), 
				ctx.currentThread.getId(), 
				ctx.nextSpanSequence);
		span.setName(name);
		ctx.setCurrentSpan(span);
		++ctx.nextSpanSequence;
	}
	
	public static void finishSpan(){
		TraceContext ctx = get();
		if(ctx==null || ctx.getCurrentSpan()==null){ return; }
		ctx.getCurrentSpan().markFinish();
		ctx.getSpans().add(ctx.getCurrentSpan());
		ctx.setCurrentSpan(null);
	}
	
	
	/****************** static collectors ******************************/
	
	public static <V> void collect(TracedCallable<V> callable){
		TraceContext ctx = get();
		if(ctx==null || callable==null){ return; }
		ctx.getThreads().addAll(CollectionTool.nullSafe(callable.getThreads()));
		ctx.getSpans().addAll(CollectionTool.nullSafe(callable.getSpans()));
//		logger.warn("collected "+CollectionTool.size(ctx.getSpans())+" spans");
	}
	
	
	/******************** ThreadLocal *******************************************************/
	
	public static ThreadLocal<TraceContext> traceContext = new ThreadLocal<TraceContext>();

	public static TraceContext bindToThread(TraceContext ctx) {
		traceContext.set(ctx);
		return ctx;
	}

	public static TraceContext get() {
		if(traceContext==null){ return null; }
		return traceContext.get();
	}

	public static void clearFromThread() {
		traceContext.set(null);
	}


	
	/*********************** get/set *********************************************/
	
	public TraceThread getCurrentThread() {
		return currentThread;
	}

	public void setCurrentThread(TraceThread currentThread) {
		this.currentThread = currentThread;
	}

	public List<TraceThread> getThreads() {
		return threads;
	}

	public void setThreads(List<TraceThread> threads) {
		this.threads = threads;
	}

	public void setTraceId(Long traceId) {
		this.traceId = traceId;
	}

	public void setTraceThreadParentId(Long traceThreadParentId) {
		this.traceThreadParentId = traceThreadParentId;
	}




	public Integer getNextSpanSequence() {
		return nextSpanSequence;
	}


	public void setNextSpanSequence(Integer nextSpanSequence) {
		this.nextSpanSequence = nextSpanSequence;
	}


	public TraceSpan getCurrentSpan() {
		return currentSpan;
	}


	public void setCurrentSpan(TraceSpan currentSpan) {
		this.currentSpan = currentSpan;
	}


	public List<TraceSpan> getSpans() {
		return spans;
	}


	public void setSpans(List<TraceSpan> spans) {
		this.spans = spans;
	}


	public String getServerId() {
		return serverId;
	}


	public void setServerId(String serverId) {
		this.serverId = serverId;
	}


	public Long getTraceId() {
		return traceId;
	}


	public Long getTraceThreadParentId() {
		return traceThreadParentId;
	}
	
	
	
}
