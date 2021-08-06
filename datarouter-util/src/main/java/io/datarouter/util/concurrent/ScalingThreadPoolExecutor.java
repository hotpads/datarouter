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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//https://github.com/kimchy/kimchy.github.com/blob/master/_posts/2008-11-23-juc-executorservice-gotcha.textile

public class ScalingThreadPoolExecutor extends DatarouterExecutorService{

	private final AtomicInteger activeCount;

	public ScalingThreadPoolExecutor(String name, int maxThreadCount){
		super(0,
				maxThreadCount,
				1,
				TimeUnit.MINUTES,
				new ScalingThreadPoolExecutorQueue(),
				new NamedThreadFactory(name, true),
				new ForceQueuePolicy());
		((ScalingThreadPoolExecutorQueue)getQueue()).setExecutor(this);
		this.activeCount = new AtomicInteger();
	}

	/**
	 * Returns the approximate number of threads that are actively executing tasks.
	 * It is approximate because a task is considered finished before this count can be decremented.
	 *
	 * @return the number of threads
	 */
	@Override
	public int getActiveCount(){
		return activeCount.get();
	}

	@Override
	protected void beforeExecute(Thread thread, Runnable runnable){
		activeCount.incrementAndGet();
		super.beforeExecute(thread, runnable);
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable){
		super.afterExecute(runnable, throwable);
		activeCount.decrementAndGet();
	}

	@SuppressWarnings("serial")
	private static class ScalingThreadPoolExecutorQueue extends LinkedBlockingQueue<Runnable>{

		private ScalingThreadPoolExecutor executor;

		private void setExecutor(ScalingThreadPoolExecutor executor){
			this.executor = executor;
		}

		@Override
		public boolean offer(Runnable runnable){
			return executor.getActiveCount() + super.size() < executor.getPoolSize() && super.offer(runnable);
		}
	}

	private static class ForceQueuePolicy implements RejectedExecutionHandler{
		@Override
		public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor){
			try{
				executor.getQueue().put(runnable);
			}catch(InterruptedException e){
				throw new RejectedExecutionException(e);
			}
		}
	}

}
