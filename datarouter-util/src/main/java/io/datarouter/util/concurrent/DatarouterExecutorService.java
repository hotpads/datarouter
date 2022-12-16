/*
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
package io.datarouter.util.concurrent;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.tracer.TracedCheckedCallable;

public class DatarouterExecutorService extends ThreadPoolExecutor{

	public static final String PREFIX_executor = "executor";

	private final Optional<String> name;

	protected DatarouterExecutorService(
			int corePoolSize,
			int maximumPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			ThreadFactory threadFactory,
			RejectedExecutionHandler handler){
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		this.name = NamedThreadFactory.findName(threadFactory);
	}

	protected DatarouterExecutorService(ThreadPoolExecutor threadPoolExecutor){
		this(threadPoolExecutor.getCorePoolSize(),
				threadPoolExecutor.getMaximumPoolSize(),
				threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS),
				TimeUnit.MILLISECONDS,
				threadPoolExecutor.getQueue(),
				threadPoolExecutor.getThreadFactory(),
				threadPoolExecutor.getRejectedExecutionHandler());
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable){
		super.afterExecute(runnable, throwable);
		name.ifPresent(execName -> Counters.inc(PREFIX_executor + " " + execName + " processed"));
	}

	@Override
	public void execute(Runnable command){
		super.execute(new TracedCheckedRunnable(command));
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
		return new DatarouterFutureTask<>(callable);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value){
		return new DatarouterFutureTask<>(runnable, value);
	}

	private static class DatarouterFutureTask<V> extends FutureTask<V>{

		public DatarouterFutureTask(Callable<V> callable){
			super(callable);
		}

		public DatarouterFutureTask(Runnable runnable, V result){
			super(runnable, result);
		}

		@Override
		public V get() throws InterruptedException, ExecutionException{
			try(TraceSpanFinisher $ = TracerTool.startSpanNoGroupType("waiting for subtask")){
				return super.get();
			}
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException{
			try(TraceSpanFinisher $ = TracerTool.startSpanNoGroupType("waiting for subtask")){
				return super.get(timeout, unit);
			}
		}

	}

	private static class TracedCheckedRunnable extends TracedCheckedCallable<Void> implements Runnable{

		static final StackWalker WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

		final Runnable runnable;

		TracedCheckedRunnable(Runnable runnable){
			super(findCaller());
			this.runnable = runnable;
		}

		@Override
		public Void wrappedCall(){
			runnable.run();
			return null;
		}

		@Override
		public void run(){
			try{
				call();
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}

		static String findCaller(){
			StackFrame frame = WALKER.walk(frames -> frames
					.skip(5)
					.findFirst())
					.get();
			return frame.getDeclaringClass().getSimpleName() + " " + frame.getMethodName();
		}
	}

}
