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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberTool;

public class FutureTool{
	private static final Logger logger = LoggerFactory.getLogger(FutureTool.class);

	/********************* multiple of different types ************/

	public static List<Future<?>> submitAllVaried(Collection<Callable<?>> callables, ExecutorService executorService){
		List<Future<?>> futures = new ArrayList<>();
		for(Callable<?> callable : IterableTool.nullSafe(callables)){
			futures.add(executorService.submit(callable));
		}
		return futures;
	}

	public static List<Object> getAllVaried(Collection<Future<?>> ins){
		List<Object> outs = ListTool.createArrayListWithSize(ins);
		for(Future<?> in : IterableTool.nullSafe(ins)){
			outs.add(get(in));
		}
		return outs;
	}

	/******************* handle multiple *************************/

	public static <T> List<T> submitAndGetAll(Collection<? extends Callable<T>> callables,
			ExecutorService executorService){
		List<Future<T>> futures = submitAll(callables, executorService);
		return getAll(futures);
	}

	public static <T> List<Future<T>> submitAll(Collection<? extends Callable<T>> callables,
			ExecutorService executorService){
		List<Future<T>> futures = new ArrayList<>();
		for(Callable<T> callable : IterableTool.nullSafe(callables)){
			futures.add(executorService.submit(callable));
		}
		return futures;
	}

	public static <T> List<T> getAll(Collection<Future<T>> ins){
		List<T> outs = ListTool.createArrayListWithSize(ins);
		for(Future<T> in : IterableTool.nullSafe(ins)){
			outs.add(get(in));
		}
		return outs;
	}

	public static <T> boolean areAllDone(Collection<Future<T>> futures){
		for(Future<T> future : futures){
			if(!future.isDone()){
				return false;
			}
		}
		return true;
	}

	public static <T> void cancelAll(Collection<Future<T>> futures){
		for(Future<T> future : futures){
			future.cancel(true);
		}
	}

	/********************* singles *****************************/

	public static <T> T submitAndGet(Callable<T> callable, ExecutorService executorService){
		return submitAndGet(callable, executorService, null);
	}

	public static <T> T submitAndGet(Callable<T> callable, ExecutorService executorService, Long timoutMilliseconds){
		return get(submit(callable, executorService), timoutMilliseconds);
	}

	public static <T> Future<T> submit(Callable<T> callable, ExecutorService executorService){
		return executorService.submit(callable);
	}

	public static <T> T get(Future<T> future){
		return get(future, null);
	}

	public static <T> T get(Future<T> future, Long timeoutMilliseconds){
		try{
			if(timeoutMilliseconds == null){
				return future.get();
			}
			return future.get(timeoutMilliseconds, TimeUnit.MILLISECONDS);
		}catch(InterruptedException | TimeoutException | ExecutionException e){
			future.cancel(true);
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> tryGetAllWithinTimeLimit(List<Future<T>> futures, long timeoutLength, TimeUnit timeUnit,
			String timeoutMessage){
		long deadlineAtMs = System.currentTimeMillis() + timeUnit.toMillis(timeoutLength);
		List<T> results = new ArrayList<>();
		for(Future<T> future : IterableTool.nullSafe(futures)){
			long remainingMs = NumberTool.max(0L, deadlineAtMs - System.currentTimeMillis());// guard for negatives
			T result = tryGet(future, remainingMs, TimeUnit.MILLISECONDS, timeoutMessage);
			if(result != null){
				results.add(result);
			}
		}
		return results;
	}

	public static <T> T tryGet(Future<T> future, long timeoutLength, TimeUnit units, String timeoutMessage){
		try{
			long nonNegativeTimeoutLength = NumberTool.max(0L, timeoutLength);// guard for negatives
			return future.get(nonNegativeTimeoutLength, units);
		}catch(InterruptedException | ExecutionException | CancellationException e){
			future.cancel(true);
			logger.error("", e);
		}catch(TimeoutException e){
			future.cancel(true);
			logger.error(timeoutMessage);
		}
		return null;
	}

	/****************** terminate ***********************/

	public static void finishAndShutdown(ExecutorService exec, long timeout, TimeUnit timeUnit){
		exec.shutdown();
		try{
			exec.awaitTermination(timeout, timeUnit);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

}
