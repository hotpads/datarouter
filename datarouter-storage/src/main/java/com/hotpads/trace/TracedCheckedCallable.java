package com.hotpads.trace;

import java.util.concurrent.Callable;

public abstract class TracedCheckedCallable<V> implements Callable<V>{

	protected final String threadName;
	protected final Thread parentThread;
	protected final Tracer parentCtx;

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

			DatarouterTracer ctx = null;
			if(shouldStartNestedTrace){
				ctx = new DatarouterTracer(parentCtx.getServerName(), parentCtx.getTraceId(), parentCtx
						.getCurrentThreadId());
				TracerThreadLocal.bindToThread(ctx);
				TracerTool.createAndStartThread(ctx, threadName);
			}

			V result = wrappedCall();
			if(Thread.currentThread().isInterrupted()){
				throw new RuntimeException();
			}

			if(shouldStartNestedTrace){
				TracerTool.finishThread(TracerThreadLocal.get());
				parentCtx.getThreads().addAll(ctx.getThreads());
				parentCtx.getSpans().addAll(ctx.getSpans());
				TracerThreadLocal.clearFromThread();
			}

			return result;
		}finally{
			Thread.currentThread().setName(originalThreadName);
		}
	}

	public abstract V wrappedCall() throws Exception;

	public static <V> TracedCheckedCallable<V> of(Callable<V> callable){
		return new FunctionalTracedCheckedCallable<>(callable);
	}

	private static class FunctionalTracedCheckedCallable<V> extends TracedCheckedCallable<V> {

		private final Callable<V> callable;

		public FunctionalTracedCheckedCallable(Callable<V> callable){
			super(callable.getClass().getName());
			this.callable = callable;
		}

		@Override
		public V wrappedCall() throws Exception{
			return callable.call();
		}

	}

}
