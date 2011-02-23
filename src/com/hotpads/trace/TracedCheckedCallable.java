package com.hotpads.trace;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public abstract class TracedCheckedCallable<V> implements Callable<V>{
	protected static Logger logger = Logger.getLogger(TracedCheckedCallable.class);

	protected String threadName;
	protected Thread parentThread;
	protected TraceContext parentCtx;
	
	
	public TracedCheckedCallable(String threadName) {
		super();
		this.threadName = threadName;
		this.parentThread = Thread.currentThread();
		this.parentCtx = TraceContext.get();
	}

	@Override
	public V call() throws Exception{
		
		Thread currentThread = Thread.currentThread();
		String originalThreadName = currentThread.getName();
		currentThread.setName(threadName);
		
		boolean hasParent = parentCtx!=null;//no use tracing if there's no parent to give them to.
		boolean isParent = parentThread.getId()==Thread.currentThread().getId();//for cases when the parent just runs the callable
		boolean shouldStartNestedTrace = hasParent && !isParent;
//		logger.warn(threadName+", shouldTrace:"+hasParent+","+isParent+","+shouldStartNestedTrace);
		
		TraceContext ctx = null;
		if(shouldStartNestedTrace){
			ctx = new TraceContext(parentCtx.getServerId(), 
					parentCtx.getTraceId(), parentCtx.getCurrentThreadId());
			TraceContext.bindToThread(ctx);
			TraceContext.createAndStartThread(threadName);
		}
		
		V v = wrappedCall();
		
		if(shouldStartNestedTrace){
			TraceContext.finishThread();
			parentCtx.getThreads().addAll(ctx.getThreads());
//			logger.warn("got threads:"+ctx.getThreads());
			parentCtx.getSpans().addAll(ctx.getSpans());
			TraceContext.clearFromThread();
		}
		
//		logger.warn(threadName+" generated "+CollectionTool.size(spans)+" spans");
		Thread.currentThread().setName(originalThreadName);
		return v;
	}



	public abstract V wrappedCall() throws Exception;
	

	public List<TraceThread> getThreads() {
		return parentCtx.getThreads();
	}

	public List<TraceSpan> getSpans() {
		return parentCtx.getSpans();
	}
	
	
	
}
