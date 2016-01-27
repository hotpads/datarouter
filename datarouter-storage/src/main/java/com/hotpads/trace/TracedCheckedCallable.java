package com.hotpads.trace;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class TracedCheckedCallable<V> implements Callable<V>{

	protected String threadName;
	protected Thread parentThread;
	protected Tracer parentCtx;
	
	
	public TracedCheckedCallable(String threadName) {
		this.threadName = threadName;
		this.parentThread = Thread.currentThread();
		this.parentCtx = TracerThreadLocal.get();
	}

	@Override
	public V call() throws Exception{
		
		Thread currentThread = Thread.currentThread();
		String originalThreadName = currentThread.getName();
		try{
			//probably don't do this because i think it screws things up with CallerRuns strategy.  need to investigate
//			currentThread.setName(threadName);
			
			boolean hasParent = parentCtx!=null;//no use tracing if there's no parent to give them to.
			boolean isParent = parentThread.getId()==Thread.currentThread().getId();//when the parent runs the callable
			boolean shouldStartNestedTrace = hasParent && !isParent;
	//		logger.warn(threadName+", shouldTrace:"+hasParent+","+isParent+","+shouldStartNestedTrace);
			
			DatarouterTracer ctx = null;
			if(shouldStartNestedTrace){
				ctx = new DatarouterTracer(parentCtx.getServerName(), parentCtx.getTraceId(), parentCtx
						.getCurrentThreadId());
				TracerThreadLocal.bindToThread(ctx);
				TracerTool.createAndStartThread(ctx, threadName);
			}
			
			V v = wrappedCall();
			if(Thread.currentThread().isInterrupted()){
				throw new RuntimeException();
			}
			
			if(shouldStartNestedTrace){
				TracerTool.finishThread(TracerThreadLocal.get());
				parentCtx.getThreads().addAll(ctx.getThreads());
	//			logger.warn("got threads:"+ctx.getThreads());
				parentCtx.getSpans().addAll(ctx.getSpans());
				TracerThreadLocal.clearFromThread();
			}
			
	//		logger.warn(threadName+" generated "+CollectionTool.size(spans)+" spans");
			return v;
		}finally{
			Thread.currentThread().setName(originalThreadName);
		}
	}



	public abstract V wrappedCall() throws Exception;
	

	public List<TraceThread> getThreads() {
		return parentCtx.getThreads();
	}

	public List<TraceSpan> getSpans() {
		return parentCtx.getSpans();
	}
	
	
	
}
