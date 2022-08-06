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
package io.datarouter.virtualnode.writebehind.base;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.virtualnode.writebehind.WriteBehindNode;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindScheduler;

public abstract class BaseWriteBehindNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends NodeOps<PK,D>>
implements WriteBehindNode<PK,D,N>{
	private static final Logger logger = LoggerFactory.getLogger(BaseWriteBehindNode.class);

	public static final int FLUSH_RATE_MS = 500;
	private static final int FLUSH_BATCH_SIZE = 100;
	private static final long DEFAULT_TIMEOUT_MS = Duration.ofMinutes(1).toMillis();

	private final ExecutorService writeExecutor;
	private final Queue<WriteWrapper<?>> queue;
	private final QueueFlusher queueFlusher;

	protected final N backingNode;
	protected final long timeoutMs;
	protected final Queue<OutstandingWriteWrapper> outstandingWrites;

	public BaseWriteBehindNode(
			DatarouterWriteBehindScheduler scheduler,
			DatarouterWriteBehindExecutor writeExecutor,
			N backingNode){
		Objects.requireNonNull(backingNode, "backingNode cannot be null.");
		this.backingNode = backingNode;
		this.writeExecutor = writeExecutor;
		this.timeoutMs = DEFAULT_TIMEOUT_MS;//1 min default
		this.outstandingWrites = new ConcurrentLinkedQueue<>();

		scheduler.scheduleWithFixedDelay(new OverdueWriteCanceller(this), 0, 1000, TimeUnit.MILLISECONDS);
		queueFlusher = new QueueFlusher();
		scheduler.scheduleWithFixedDelay(queueFlusher, 500, FLUSH_RATE_MS, TimeUnit.MILLISECONDS);

		queue = new LinkedBlockingQueue<>();
	}

	@Override
	public Queue<WriteWrapper<?>> getQueue(){
		return queue;
	}

	@Override
	public N getBackingNode(){
		return backingNode;
	}

	public void flush(){
		Scanner.of(queueFlusher.flushQueue())
				.include(Objects::nonNull)
				.forEach(FutureTool::get);
	}

	private class QueueFlusher implements Runnable{

		private WriteWrapper<Object> previousWriteWrapper;

		@Override
		public void run(){
			try{
				List<Future<?>> futures = flushQueue();
				logger.info("Futures submited count={} node={}", futures.size(), backingNode);
			}catch(Throwable t){
				logger.error("Failed to flush queue for {}", backingNode, t);
			}
		}

		private synchronized List<Future<?>> flushQueue(){
			List<Future<?>> futures = new ArrayList<>();
			previousWriteWrapper = null;
			while(!queue.isEmpty()){
				WriteWrapper<?> writeWrapper = queue.poll();
				if(previousWriteWrapper != null && (!writeWrapper.getOp().equals(previousWriteWrapper.getOp())
						|| writeWrapper.getConfig() != null)){
					futures.add(handlePrevious());
				}
				if(writeWrapper.getConfig() != null){
					futures.add(handleWriteWrapper(writeWrapper));
				}else{
					List<?> list = writeWrapper.getObjects();
					if(previousWriteWrapper == null){
						previousWriteWrapper = new WriteWrapper<>(writeWrapper.getOp(), Collections.emptyList(), null);
					}
					int previousSize = previousWriteWrapper.getObjects().size();
					int end = Math.min(FLUSH_BATCH_SIZE - previousSize, list.size());
					previousWriteWrapper.getObjects().addAll(list.subList(0, end));
					if(previousWriteWrapper.getObjects().size() == FLUSH_BATCH_SIZE){
						futures.add(handlePrevious());
					}
					int counter = 1;
					while(counter * FLUSH_BATCH_SIZE - previousSize < list.size()){
						int beginning = counter * FLUSH_BATCH_SIZE - previousSize;
						end = Math.min(++counter * FLUSH_BATCH_SIZE - previousSize, list.size());
						if(previousWriteWrapper == null){
							previousWriteWrapper = new WriteWrapper<>(writeWrapper.getOp(), Collections.emptyList(),
									null);
						}
						previousWriteWrapper.getObjects().addAll(list.subList(beginning, end));
						if(previousWriteWrapper.getObjects().size() == FLUSH_BATCH_SIZE){
							futures.add(handlePrevious());
						}
					}
				}
			}
			if(previousWriteWrapper != null){
				futures.add(handlePrevious());// don't forget the last batch
			}
			return futures;
		}

		private Future<?> handlePrevious(){
			Future<?> future = handleWriteWrapper(previousWriteWrapper);
			previousWriteWrapper = null;
			return future;
		}

		private Future<?> handleWriteWrapper(WriteWrapper<?> writeWrapper){
			Collection<?> databeans = writeWrapper.getObjects();
			if(databeans == null || databeans.isEmpty()){
				return null;
			}
			String opDesc = String.format("%s with %s %s", writeWrapper.getOp(), databeans.size(),
					databeans.iterator().next().getClass().getSimpleName());
			WriteWrapper<?> writeWrapperClone = writeWrapper.clone();
			Future<?> future = writeExecutor.submit(() -> {
				try{
					if(!handleWriteWrapperInternal(writeWrapperClone)){
						logger.error("unhandled op desc={}", opDesc);
					}
				}catch(Throwable t){
					logger.error("opDesc={}", opDesc, t);
				}
			});
			outstandingWrites.add(new OutstandingWriteWrapper(System.currentTimeMillis(), future, opDesc));
			return future;
		}

	}

}
