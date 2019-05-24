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
package io.datarouter.storage.trace.callable;

import java.util.concurrent.Callable;

import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.trace.tracer.DatarouterTracer;

public abstract class TracedCheckedCallable<V> implements Callable<V>{

	protected final String traceThreadName;
	protected final Thread parentThread;
	protected final Tracer parentTracer;
	private final long queueTimeMs;

	public TracedCheckedCallable(String traceThreadName){
		this.traceThreadName = traceThreadName;
		this.parentThread = Thread.currentThread();
		this.parentTracer = TracerThreadLocal.get();
		this.queueTimeMs = System.currentTimeMillis(); // approximate queue time with task creation time
	}

	@Override
	public V call() throws Exception{
		Thread currentThread = Thread.currentThread();
		String originalThreadName = currentThread.getName();
		try{
			//probably don't do this because i think it screws things up with CallerRuns strategy.  need to investigate
//			currentThread.setName(threadName);

			boolean hasParent = parentTracer != null; //no use tracing if there's no parent to give them to.

			//when the parent runs the callable
			boolean isParent = parentThread.getId() == currentThread.getId();
			boolean shouldStartNestedTrace = hasParent && !isParent;

			DatarouterTracer ctx = null;
			if(shouldStartNestedTrace){
				ctx = new DatarouterTracer(parentTracer.getServerName(), parentTracer.getTraceId(), parentTracer
						.getCurrentThreadId());
				TracerThreadLocal.bindToThread(ctx);
				TracerTool.createAndStartThread(ctx, traceThreadName, queueTimeMs);
			}

			V result = wrappedCall();
			if(Thread.currentThread().isInterrupted()){
				throw new InterruptedException();
			}

			if(shouldStartNestedTrace){
				TracerTool.finishThread(TracerThreadLocal.get());
				parentTracer.getThreads().addAll(ctx.getThreads());
				parentTracer.getSpans().addAll(ctx.getSpans());
				TracerThreadLocal.clearFromThread();
			}

			return result;
		}finally{
			Thread.currentThread().setName(originalThreadName);
		}
	}

	public abstract V wrappedCall() throws Exception;

	public static <V> TracedCheckedCallable<V> of(String threadName, Callable<V> callable){
		return new FunctionalTracedCheckedCallable<>(threadName, callable);
	}

	public static <V> TracedCheckedCallable<V> of(Callable<V> callable){
		return new FunctionalTracedCheckedCallable<>(callable);
	}

	private static class FunctionalTracedCheckedCallable<V> extends TracedCheckedCallable<V>{

		private final Callable<V> callable;

		public FunctionalTracedCheckedCallable(Callable<V> callable){
			this(callable.getClass().getName(), callable);
		}

		public FunctionalTracedCheckedCallable(String threadName, Callable<V> callable){
			super(threadName);
			this.callable = callable;
		}

		@Override
		public V wrappedCall() throws Exception{
			return callable.call();
		}

	}

}
