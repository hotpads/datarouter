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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefetchingScanner<T> extends BaseLinkedScanner<T,T>{
	private static final Logger logger = LoggerFactory.getLogger(PrefetchingScanner.class);

	private final ExecutorService exec;
	private final BlockingQueue<PrefetchMessage<T>> blockingQueue;
	private boolean started;
	private Future<Void> prefetchFuture;

	public PrefetchingScanner(
			Scanner<T> input,
			ExecutorService exec,
			int queueCapacity){
		super(input);
		this.exec = exec;
		blockingQueue = new LinkedBlockingQueue<>(queueCapacity);
		started = false;
	}

	@Override
	public boolean advanceInternal(){
		if(!started){
			prefetchFuture = exec.submit(() -> prefetch());
			started = true;
		}
		PrefetchMessage<T> next = blockingQueueTake(blockingQueue);
		if(next.isError()){
			throw next.error();
		}
		current = next.value();
		return next.isPresent();
	}

	private Void prefetch(){
		try{
			Scanner.generate(input::next)
					.advanceWhile(ScannerNextItem::isPresent)
					.forEach(next -> blockingQueuePut(blockingQueue, PrefetchMessage.present(next.value())));
			// Add non-present item to signal the end.
			blockingQueuePut(blockingQueue, PrefetchMessage.absent());
		}catch(RuntimeException e){
			blockingQueuePut(blockingQueue, PrefetchMessage.error(e));
		}
		return null;
	}

	@Override
	protected void closeInternal(){
		if(prefetchFuture != null){
			try{
				prefetchFuture.cancel(true);
			}catch(Exception e){
				logger.warn("scanner exception on prefetchFuture.cancel", e);
			}
		}
	}

	/*-------- queue message -----------*/

	private record PrefetchMessage<T>(
			boolean isPresent,
			T value,
			boolean isError,
			RuntimeException error){

		private static <T> PrefetchMessage<T> present(T item){
			return new PrefetchMessage<>(true, item, false, null);
		}

		private static <T> PrefetchMessage<T> absent(){
			return new PrefetchMessage<>(false, null, false, null);
		}

		private static <T> PrefetchMessage<T> error(RuntimeException error){
			return new PrefetchMessage<>(false, null, true, error);
		}
	}

	/*-------- uncheck exceptions ----------*/

	private static <T> void blockingQueuePut(BlockingQueue<T> blockingQueue, T message){
		try{
			blockingQueue.put(message);
		}catch(InterruptedException e){
			throw new RuntimeException("", e);
		}
	}

	private static <T> T blockingQueueTake(BlockingQueue<T> blockingQueue){
		try{
			return blockingQueue.take();
		}catch(InterruptedException e){
			throw new RuntimeException("", e);
		}
	}

}
