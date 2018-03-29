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

import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import io.datarouter.util.exception.InterruptedRuntimeException;

public class CompletionServiceTool{

	public static void callAllInSingleUseExecutor(Stream<? extends Callable<?>> callables, int numThreads){
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		callAll(executor, callables, numThreads);
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(5));
	}

	public static void callAll(ExecutorService executor, Stream<? extends Callable<?>> callables, int numThreads){
		Iterator<?> resultIterator = createResultIterator(executor, callables.iterator(), numThreads);
		resultIterator.forEachRemaining($ -> {});
	}

	public static <T> Iterator<T> createResultIterator(ExecutorService executor,
			Iterator<? extends Callable<T>> callableIterator, int numThreads){
		return new ResultIterator<>(executor, callableIterator, numThreads);
	}

	private static class ResultIterator<T> implements Iterator<T>{
		private final CompletionService<T> completionService;
		private final Iterator<? extends Callable<T>> callableIterator;
		private final Set<Future<T>> runningFutures;

		public ResultIterator(ExecutorService executor, Iterator<? extends Callable<T>> callableIterator,
				int numThreads){
			this.completionService = new ExecutorCompletionService<>(executor);
			this.callableIterator = callableIterator;
			this.runningFutures = new HashSet<>();

			//submit the first x callables
			while(runningFutures.size() < numThreads && callableIterator.hasNext()){
				runningFutures.add(completionService.submit(callableIterator.next()));
			}
		}

		@Override
		public boolean hasNext(){
			return runningFutures.size() > 0;
		}

		@Override
		public T next(){
			Future<T> future = null;
			T result;
			try{
				future = completionService.take();
				result = future.get();
			}catch(InterruptedException e){
				runningFutures.forEach(runningFuture -> runningFuture.cancel(true));
				runningFutures.clear();
				Thread.currentThread().interrupt();
				throw new InterruptedRuntimeException(e);
			}catch(ExecutionException e){
				throw new RuntimeException(e);
			}finally{
				if(future != null){
					runningFutures.remove(future);
				}
				if(!Thread.currentThread().isInterrupted() && callableIterator.hasNext()){
					runningFutures.add(completionService.submit(callableIterator.next()));
				}
			}
			return result;
		}

	}

}
