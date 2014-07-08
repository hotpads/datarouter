package com.hotpads.datarouter.node.type.writebehind.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.node.type.writebehind.mixin.WriteWrapper;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseWriteBehindNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,DatabeanFielder<PK,D>>{

	private static final int FLUSH_BATCH_SIZE = 100;

	public static final int DEFAULT_WRITE_BEHIND_THREADS = 1;
	public static final long DEFAULT_TIMEOUT_MS = 60*1000;
	
	protected N backingNode;
	protected ExecutorService writeExecutor;
	protected long timeoutMs;//TODO also limit by queue length
	protected Queue<OutstandingWriteWrapper> outstandingWrites;
	protected ScheduledExecutorService cancelExecutor;
	private ScheduledExecutorService flushScheduler;
	private Queue<WriteWrapper<?>> queue;

	
	public BaseWriteBehindNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(new NodeParamsBuilder<PK,D,DatabeanFielder<PK,D>>(router, databeanClass).build());
		if(backingNode==null){ throw new IllegalArgumentException("backingNode cannont be null."); }
		this.backingNode = backingNode;
		
		if(writeExecutor!=null){
			this.writeExecutor = writeExecutor;
		}else{
			this.writeExecutor = Executors.newFixedThreadPool(DEFAULT_WRITE_BEHIND_THREADS);
			this.writeExecutor.submit(new Callable<Void>(){
				public Void call(){
					Thread.currentThread().setName("NonBlockingWriteNode flusher:"+getName());
					return null; 
				}
			});
		}
		
		this.timeoutMs = DEFAULT_TIMEOUT_MS;//1 min default
		this.outstandingWrites = new ConcurrentLinkedQueue<OutstandingWriteWrapper>();
		
		if(cancelExecutor!=null){
			this.cancelExecutor = cancelExecutor;
			this.cancelExecutor.scheduleWithFixedDelay(new OverdueWriteCanceller(this), 0, 1000, TimeUnit.MILLISECONDS); 
		}else{
			this.cancelExecutor = Executors.newSingleThreadScheduledExecutor();
			this.cancelExecutor.scheduleWithFixedDelay(new OverdueWriteCanceller(this), 0, 1000, TimeUnit.MILLISECONDS); 
			this.cancelExecutor.submit(new Callable<Void>(){
				public Void call(){
					Thread.currentThread().setName("NonBlockingWriteNode canceller:"+getName());
					return null; 
				}
			});
		}
		

		this.flushScheduler = Executors.newSingleThreadScheduledExecutor();
		this.flushScheduler.scheduleWithFixedDelay(new QueuFlucher(), 500, 500, TimeUnit.MILLISECONDS);
		this.flushScheduler.submit(new Callable<Void>(){
			public Void call(){
				Thread.currentThread().setName("NonBlockingWriteNode writer:"+getName());
				return null; 
			}
		});
		
		queue = new LinkedBlockingQueue<WriteWrapper<?>>();
	}
	
	
	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		names.addAll(CollectionTool.nullSafe(getName()));
		names.addAll(CollectionTool.nullSafe(backingNode.getAllNames()));
		return names;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		all.addAll(ListTool.nullSafe(backingNode.getPhysicalNodes()));
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		all.addAll(ListTool.nullSafe(backingNode.getPhysicalNodesForClient(clientName)));
		return all;
	}

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		SetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		return backingNode.usesClient(clientName);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = SetTool.createHashSet();
		clientNames.addAll(CollectionTool.nullSafe(backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public void clearThreadSpecificState(){
		backingNode.clearThreadSpecificState();
	}

	@Override
	public List<N> getChildNodes(){
		return ListTool.wrap(backingNode);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	public Queue<WriteWrapper<?>> getQueue(){
		return queue;
	}

	public N getBackingNode(){
		return backingNode;
	}

	public class QueuFlucher implements Runnable{

		private WriteWrapper<Object> previousWriteWrapper;

		@Override
		public void run(){
			previousWriteWrapper = new WriteWrapper<Object>();
			while(CollectionTool.notEmpty(queue)){
				WriteWrapper<?> writeWrapper = queue.poll();
				if(!writeWrapper.getOp().equals(previousWriteWrapper.getOp()) || writeWrapper.getConfig() != null){
					handlePrevious();
				}
				if(writeWrapper.getConfig() != null){
					handleWriteWrapper(writeWrapper);
				}else{
					List<?> list = asList(writeWrapper.getObjects());
					int previousSize = previousWriteWrapper.getObjects().size();
					int end = Math.min(FLUSH_BATCH_SIZE - previousSize, list.size());
					previousWriteWrapper.getObjects().addAll(list.subList(0, end));
					previousWriteWrapper.setOp(writeWrapper.getOp());
					if(previousWriteWrapper.getObjects().size() == FLUSH_BATCH_SIZE){
						handlePrevious();
					}
					int i = 1;
					while(i * FLUSH_BATCH_SIZE - previousSize < list.size()){
						int beginning = i * FLUSH_BATCH_SIZE - previousSize;
						end = Math.min(++i * FLUSH_BATCH_SIZE - previousSize, list.size());
						previousWriteWrapper.getObjects().addAll(list.subList(beginning, end));
						previousWriteWrapper.setOp(writeWrapper.getOp());
						if(previousWriteWrapper.getObjects().size() == FLUSH_BATCH_SIZE){
							handlePrevious();
						}
					}
				}
			}
			handlePrevious();// don't forget the last batch
		}

		private <T>List<T> asList(Collection<T> coll){ // TODO should be in CollectionTool?
			if(coll instanceof List){
				return (List<T>)coll;
			}else{
				return new ArrayList<T>(coll);
			}
		}

		private void handlePrevious(){
			if(previousWriteWrapper.getOp() != null){
				handleWriteWrapper(previousWriteWrapper);
			}
			previousWriteWrapper = new WriteWrapper<>();
		}

		private void handleWriteWrapper(final WriteWrapper<?> writeWrapper){
			if(CollectionTool.notEmpty(writeWrapper.getObjects())){
				outstandingWrites.add(new OutstandingWriteWrapper(System.currentTimeMillis(), writeExecutor
						.submit(new Callable<Void>(){

							public Void call(){
								try{
									if(!handlewriteWrapperInternal(writeWrapper)){
										logger.error("Not able to handle this op: " + writeWrapper.getOp());
									}
								}catch(Exception e){
									logger.error("error on " + writeWrapper.getOp() + " with "
											+ writeWrapper.getObjects().size() + " element(s)", e);
								}
								return null;
							}
						})));
			}
		}

	}

	protected abstract boolean handlewriteWrapperInternal(WriteWrapper<?> writeWrapper);
}
