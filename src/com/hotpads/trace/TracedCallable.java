package com.hotpads.trace;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public abstract class TracedCallable<V> implements Callable<V>{
	protected static Logger logger = Logger.getLogger(TracedCallable.class);

	protected String threadName;
	protected String serverId;
	protected Long traceId;
	protected Long traceThreadParentId;
	
	protected List<TraceThread> threads;
	protected List<TraceSpan> spans;
	
	
	
	public TracedCallable(String threadName) {
		super();
		this.threadName = threadName;
		TraceContext ctx = TraceContext.get();
		this.serverId = ctx.getServerId();
		this.traceId = ctx.getTraceId();
		this.traceThreadParentId = ctx.getCurrentThreadId();//to be set in the child
		
		this.threads = ctx.getThreads();
		this.spans = ctx.getSpans();
	}

	@Override
	public V call() throws Exception{
		TraceContext ctx = new TraceContext(
				this.serverId, this.traceId, this.traceThreadParentId);
		TraceContext.bindToThread(ctx);
		
		Thread currentThread = Thread.currentThread();
		String originalThreadName = currentThread.getName();
		currentThread.setName(this.threadName);
		
		//begin wrapped
		TraceContext.createAndStartThread(threadName, this.traceThreadParentId);
		V v = wrappedCall();
		TraceContext.finishThread();
		//end wrapped
		
		threads.addAll(ctx.getThreads());
		spans.addAll(ctx.getSpans());
//		logger.warn(threadName+" generated "+CollectionTool.size(spans)+" spans");
		TraceContext.clearFromThread();
		Thread.currentThread().setName(originalThreadName);
		return v;
	}



	public abstract V wrappedCall() throws Exception;
	

	public List<TraceThread> getThreads() {
		return threads;
	}

	public List<TraceSpan> getSpans() {
		return spans;
	}
	
	
	
}
