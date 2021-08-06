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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefetchingScanner<T> extends BaseLinkedScanner<T,List<T>>{
	private static final Logger logger = LoggerFactory.getLogger(PrefetchingScanner.class);

	private final ExecutorService exec;
	private final int batchSize;

	private Future<List<T>> nextFuture;

	public PrefetchingScanner(Scanner<T> input, ExecutorService exec, int batchSize){
		super(input);
		this.exec = exec;
		this.batchSize = batchSize;
		this.nextFuture = submitNext();
	}

	@Override
	public boolean advanceInternal(){
		if(nextFuture == null){
			current = null;
			return false;
		}
		current = get(nextFuture);
		nextFuture = null;
		if(current.isEmpty()){
			current = null;
			return false;
		}
		if(current.size() == batchSize){
			nextFuture = submitNext();
		}
		return true;
	}

	private Future<List<T>> submitNext(){
		return exec.submit(() -> input.take(batchSize));
	}

	@Override
	protected void closeInternal(){
		if(nextFuture != null){
			try{
				nextFuture.cancel(true);
			}catch(Exception e){
				logger.warn("scanner exception on nextFuture.cancel", e);
			}
		}
	}

	private static <T> T get(Future<T> future){
		try{
			return future.get();
		}catch(InterruptedException | ExecutionException e){
			future.cancel(true);
			throw new RuntimeException("", e);
		}
	}

}
