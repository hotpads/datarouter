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
package io.datarouter.scanner;

import java.util.LinkedHashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ParallelMappingScanner<T,R> extends BaseScanner<R>{

	private final Scanner<T> input;
	private final Function<? super T,? extends R> mapper;
	private final ExecutorService executor;
	private final boolean allowUnorderedResults;
	private final LinkedHashSet<Future<R>> runningFutures;
	private final CompletionService<R> completionService;

	public ParallelMappingScanner(
			Scanner<T> input,
			Threads threads,
			boolean allowUnorderedResults,
			Function<? super T,? extends R> mapper){
		this.input = input;
		this.mapper = mapper;
		this.executor = threads.exec();
		this.allowUnorderedResults = allowUnorderedResults;
		this.runningFutures = new LinkedHashSet<>();
		this.completionService = allowUnorderedResults ? new ExecutorCompletionService<>(executor) : null;
		submitCallables(threads.count());
	}

	@Override
	public boolean advance(){
		if(runningFutures.isEmpty()){
			current = null;
			return false;
		}
		try{
			current = nextResult();
			return true;
		}catch(RuntimeException e){
			runningFutures.forEach(runningFuture -> runningFuture.cancel(true));
			throw e;
		}
	}

	private void submitCallables(int limit){
		for(int i = 0; i < limit; i++){
			if(input.advance()){
				T item = input.current();
				Callable<R> callable = makeCallable(item);
				submitCallable(callable);
			}else{
				return;
			}
		}
	}

	private Callable<R> makeCallable(T item){
		return () -> mapper.apply(item);
	}

	private void submitCallable(Callable<R> callable){
		Future<R> future = allowUnorderedResults
				? completionService.submit(callable)
				: executor.submit(callable);
		runningFutures.add(future);
	}

	private R nextResult(){
		try{
			Future<R> future = nextFuture();
			runningFutures.remove(future);
			submitCallables(1);
			return future.get();
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}catch(ExecutionException e){
			throw new RuntimeException(e);
		}
	}

	private Future<R> nextFuture() throws InterruptedException{
		return allowUnorderedResults
				? completionService.take()
				: runningFutures.iterator().next();
	}

}
