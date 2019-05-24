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
package io.datarouter.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DatarouterExecutorService implements ExecutorService{

	private final ThreadPoolExecutor threadPoolExecutor;

	public DatarouterExecutorService(ThreadPoolExecutor executorService){
		this.threadPoolExecutor = executorService;
	}

	public ThreadPoolExecutor getThreadPoolExecutor(){
		return threadPoolExecutor;
	}

	@Override
	public void execute(Runnable command){
		threadPoolExecutor.execute(command);
	}

	@Override
	public void shutdown(){
		threadPoolExecutor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow(){
		return threadPoolExecutor.shutdownNow();
	}

	@Override
	public boolean isShutdown(){
		return threadPoolExecutor.isShutdown();
	}

	@Override
	public boolean isTerminated(){
		return threadPoolExecutor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException{
		return threadPoolExecutor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task){
		return threadPoolExecutor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result){
		return threadPoolExecutor.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task){
		return threadPoolExecutor.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException{
		return threadPoolExecutor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	throws InterruptedException{
		return threadPoolExecutor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException{
		return threadPoolExecutor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	throws InterruptedException, ExecutionException, TimeoutException{
		return threadPoolExecutor.invokeAny(tasks, timeout, unit);
	}

}
