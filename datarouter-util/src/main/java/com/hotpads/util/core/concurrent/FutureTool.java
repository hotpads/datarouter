package com.hotpads.util.core.concurrent;

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

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;

public class FutureTool {
	private static final Logger logger = LoggerFactory.getLogger(FutureTool.class);

	/******************** sequential ***************************/

	//these are in CallableTool

//	public static <T> List<T> callAll(Collection<Callable<T>> callables){
//		List<T> outs = new ArrayList<>();
//		for(Callable<T> callable : IterableTool.nullSafe(callables)){
//			outs.add(call(callable));
//		}
//		return outs;
//	}
//
//	public static <T> T call(Callable<T> callable){
//		try{
//			return callable.call();
//		}catch(Exception e){
//			throw new RuntimeException(e);
//		}
//	}

	/********************* multiple of different types ************/

	public static List<?> submitAndGetAllVaried(Collection<Callable<?>> callables, ExecutorService executorService){
		List<Future<?>> futures = submitAllVaried(callables, executorService);
		return getAllVaried(futures);
	}

	public static List<Future<?>> submitAllVaried(Collection<Callable<?>> callables, ExecutorService executorService){
		List<Future<?>> futures = new ArrayList<>();
		for(Callable<?> callable : DrIterableTool.nullSafe(callables)){
			futures.add(executorService.submit(callable));
		}
		return futures;
	}

	public static List<?> getAllVaried(Collection<Future<?>> ins){
		List<?> outs = DrListTool.createArrayListWithSize(ins);
		for(Future<?> in : DrIterableTool.nullSafe(ins)){
			get(in);
		}
		return outs;
	}


	/******************* handle multiple *************************/

	public static <T>List<T> submitAndGetAll(Collection<? extends Callable<T>> callables, ExecutorService executorService){
		List<Future<T>> futures = submitAll(callables, executorService);
		return getAll(futures);
	}

	public static <T>List<Future<T>> submitAll(Collection<? extends Callable<T>> callables, ExecutorService executorService){
		List<Future<T>> futures = new ArrayList<>();
		for(Callable<T> callable : DrIterableTool.nullSafe(callables)){
			futures.add(executorService.submit(callable));
		}
		return futures;
	}

	public static <T>List<T> getAll(Collection<Future<T>> ins){
		List<T> outs = DrListTool.createArrayListWithSize(ins);
		for(Future<T> in : DrIterableTool.nullSafe(ins)){
			outs.add(get(in));
		}
		return outs;
	}

	public static <T>boolean areAllDone(Collection<Future<T>> futures){
		for(Future<T> future : futures){
			if(!future.isDone()){
				return false;
			}
		}
		return true;
	}

	public static <T>void cancelAll(Collection<Future<T>> futures){
		for(Future<T> future : futures){
			future.cancel(true);
		}
	}
	/********************* singles *****************************/

	public static <T>T submitAndGet(Callable<T> callable, ExecutorService executorService){
		return submitAndGet(callable, executorService, null);
	}

	public static <T>T submitAndGet(Callable<T> callable, ExecutorService executorService, Long timoutMilliseconds){
		return get(submit(callable, executorService), timoutMilliseconds);
	}

	public static <T>Future<T> submit(Callable<T> callable, ExecutorService executorService){
		return executorService.submit(callable);
	}

	public static <T>T get(Future<T> future){
		return get(future, null);
	}

	public static <T> T get(Future<T> future, Long timeoutMilliseconds){
		try{
			if(timeoutMilliseconds==null){
				return future.get();
			}
			return future.get(timeoutMilliseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			future.cancel(true);
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> tryGetAllWithinTimeLimit(List<Future<T>> futures, long timeoutLength, TimeUnit timeUnit,
			String timeoutMessage){
		long deadlineAtMs = System.currentTimeMillis() + timeUnit.toMillis(timeoutLength);
		List<T> results = new ArrayList<>();
		for(Future<T> future : DrIterableTool.nullSafe(futures)){
			long remainingMs = DrNumberTool.max(0L, deadlineAtMs - System.currentTimeMillis());//guard for negatives
			T result = tryGet(future, remainingMs, TimeUnit.MILLISECONDS, timeoutMessage);
			if(result != null){
				results.add(result);
			}
		}
		return results;
	}

	public static <T> T tryGet(Future<T> future, long timeoutLength, TimeUnit units, String timeoutMessage) {
		try{
			long nonNegativeTimeoutLength = DrNumberTool.max(0L, timeoutLength);//guard for negatives
			return future.get(nonNegativeTimeoutLength, units);
		}catch(InterruptedException|ExecutionException|CancellationException e){
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
