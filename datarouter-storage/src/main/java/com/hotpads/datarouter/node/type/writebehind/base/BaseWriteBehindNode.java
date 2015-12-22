package com.hotpads.datarouter.node.type.writebehind.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.util.core.concurrent.FutureTool;

public abstract class BaseWriteBehindNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>
extends BaseNode<PK,D,DatabeanFielder<PK,D>>
implements WriteBehindNode<PK,D,N>{

	private static final int FLUSH_BATCH_SIZE = 100;
	private static final long DEFAULT_TIMEOUT_MS = 60*1000;

	protected N backingNode;
	protected long timeoutMs;//TODO also limit by queue length
	protected Queue<OutstandingWriteWrapper> outstandingWrites;
	private ExecutorService writeExecutor;
	private Queue<WriteWrapper<?>> queue;
	private BaseWriteBehindNode<PK,D,N>.QueueFlusher queueFlusher;


	public BaseWriteBehindNode(Supplier<D> databeanSupplier, Router router, N backingNode){
		super(new NodeParamsBuilder<>(router, databeanSupplier).build());
		if(backingNode==null){
			throw new IllegalArgumentException("backingNode cannot be null.");
		}
		this.backingNode = backingNode;

		this.writeExecutor = router.getContext().getWriteBehindExecutor();

		this.timeoutMs = DEFAULT_TIMEOUT_MS;//1 min default
		this.outstandingWrites = new ConcurrentLinkedQueue<>();

		ScheduledExecutorService scheduler = router.getContext().getWriteBehindScheduler();
		scheduler.scheduleWithFixedDelay(new OverdueWriteCanceller(this), 0, 1000, TimeUnit.MILLISECONDS);
		queueFlusher = new QueueFlusher();
		scheduler.scheduleWithFixedDelay(queueFlusher, 500, 500, TimeUnit.MILLISECONDS);

		queue = new LinkedBlockingQueue<>();
	}


	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = new HashSet<>();
		names.addAll(DrCollectionTool.nullSafe(getName()));
		names.addAll(DrCollectionTool.nullSafe(backingNode.getAllNames()));
		return names;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
		all.addAll(DrListTool.nullSafe(backingNode.getPhysicalNodes()));
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
		all.addAll(DrListTool.nullSafe(backingNode.getPhysicalNodesForClient(clientName)));
		return all;
	}

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = new TreeSet<>();
		DrSetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>();
		clientIds.addAll(backingNode.getClientIds());
		return new ArrayList<>(clientIds);
	}


	@Override
	public boolean usesClient(String clientName){
		return backingNode.usesClient(clientName);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = new HashSet<>();
		clientNames.addAll(DrCollectionTool.nullSafe(backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public List<N> getChildNodes(){
		return DrListTool.wrap(backingNode);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
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
			if(DrCollectionTool.isEmpty(writeWrapper.getObjects())){
				return null;
			}
			// cloning to prevent from concurrency issues
			final WriteWrapper<?> writeWrapperClone = new WriteWrapper<>(writeWrapper);
			Future<Void> future = writeExecutor.submit(new Callable<Void>(){

				@Override
				public Void call(){
					try{
						if(!handleWriteWrapperInternal(writeWrapperClone)){
							logger.error("Not able to handle this op: " + writeWrapperClone.getOp());
						}
					}catch(Exception e){
						logger.error("error on " + writeWrapperClone.getOp() + " with "
								+ writeWrapperClone.getObjects().size() + " element(s)", e);
					}
					return null;
				}
			});
			outstandingWrites.add(new OutstandingWriteWrapper(System.currentTimeMillis(), future));
			return future;
		}

	}

}
