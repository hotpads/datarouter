package com.hotpads.datarouter.node.type.writebehind.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.concurrent.FutureTool;

public abstract class BaseWriteBehindNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends NodeOps<PK,D>>
implements WriteBehindNode<PK,D,N>{

	public static final int FLUSH_RATE_MS = 500;
	private static final int FLUSH_BATCH_SIZE = 100;
	private static final long DEFAULT_TIMEOUT_MS = 60 * 1000;

	protected N backingNode;
	protected long timeoutMs;//TODO also limit by queue length
	protected Queue<OutstandingWriteWrapper> outstandingWrites;
	private ExecutorService writeExecutor;
	private Queue<WriteWrapper<?>> queue;
	private BaseWriteBehindNode<PK,D,N>.QueueFlusher queueFlusher;


	public BaseWriteBehindNode(Datarouter datarouter, N backingNode){
		if(backingNode == null){
			throw new IllegalArgumentException("backingNode cannot be null.");
		}
		this.backingNode = backingNode;

		this.writeExecutor = datarouter.getWriteBehindExecutor();

		this.timeoutMs = DEFAULT_TIMEOUT_MS;//1 min default
		this.outstandingWrites = new ConcurrentLinkedQueue<>();

		ScheduledExecutorService scheduler = datarouter.getWriteBehindScheduler();
		scheduler.scheduleWithFixedDelay(new OverdueWriteCanceller(this), 0, 1000, TimeUnit.MILLISECONDS);
		queueFlusher = new QueueFlusher();
		scheduler.scheduleWithFixedDelay(queueFlusher, 500, FLUSH_RATE_MS, TimeUnit.MILLISECONDS);

		queue = new LinkedBlockingQueue<>();
	}


	/*************************** node methods *************************/

	@Override
	public Queue<WriteWrapper<?>> getQueue(){
		return queue;
	}

	@Override
	public N getBackingNode(){
		return backingNode;
	}

	public void flush(){
		List<Future<Void>> flushQueue = queueFlusher.flushQueue();
		for(Future<Void> future : flushQueue){
			if(future != null){
				FutureTool.get(future);
			}
		}
	}

	private class QueueFlusher implements Runnable{
		private final Logger logger = LoggerFactory.getLogger(BaseWriteBehindNode.QueueFlusher.class);

		private WriteWrapper<Object> previousWriteWrapper;

		@Override
		public void run(){
			try{
				flushQueue();
			}catch(Exception e){
				logger.error("Failed to flush queue", e);
				throw e;
			}
		}

		private List<Future<Void>> flushQueue(){
			List<Future<Void>> futures = new ArrayList<>();
			previousWriteWrapper = null;
			while(DrCollectionTool.notEmpty(queue)){
				WriteWrapper<?> writeWrapper = queue.poll();
				if(previousWriteWrapper != null && (!writeWrapper.getOp().equals(previousWriteWrapper.getOp())
						|| writeWrapper.getConfig() != null)){
					futures.add(handlePrevious());
				}
				if(writeWrapper.getConfig() != null){
					futures.add(handleWriteWrapper(writeWrapper));
				}else{
					List<?> list = DrListTool.asList(writeWrapper.getObjects());
					if(previousWriteWrapper == null){
						previousWriteWrapper = new WriteWrapper<>(writeWrapper.getOp(), new LinkedList<>(), null);
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
							previousWriteWrapper = new WriteWrapper<>(writeWrapper.getOp(), new LinkedList<>(), null);
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

		private Future<Void> handlePrevious(){
			Future<Void> future = handleWriteWrapper(previousWriteWrapper);
			previousWriteWrapper = null;
			return future;
		}

		private Future<Void> handleWriteWrapper(WriteWrapper<?> writeWrapper){
			Collection<?> databeans = writeWrapper.getObjects();
			if(DrCollectionTool.isEmpty(databeans)){
				return null;
			}

			String opDesc = writeWrapper.getOp() + " with " + databeans.size() + " " + DrCollectionTool.getFirst(
					databeans).getClass().getSimpleName();
			// cloning to prevent from concurrency issues
			WriteWrapper<?> writeWrapperClone = new WriteWrapper<>(writeWrapper);
			Future<Void> future = writeExecutor.submit(() -> {
				try{
					if(!handleWriteWrapperInternal(writeWrapperClone)){
						logger.error("Not able to handle this op: " + writeWrapperClone.getOp());
					}
				}catch(Exception e){
					logger.error("error on {}", opDesc, e);
				}
				return null;
			});
			outstandingWrites.add(new OutstandingWriteWrapper(System.currentTimeMillis(), future, opDesc));
			return future;
		}

	}

}
