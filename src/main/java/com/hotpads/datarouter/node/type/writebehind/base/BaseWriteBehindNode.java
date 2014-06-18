package com.hotpads.datarouter.node.type.writebehind.base;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
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
	
	public static final int DEFAULT_WRITE_BEHIND_THREADS = 1;
	public static final long DEFAULT_TIMEOUT_MS = 60*1000;
	
	protected N backingNode;
	protected ExecutorService writeExecutor;
	protected long timeoutMs;//TODO also limit by queue length
	protected Queue<OutstandingWriteWrapper> outstandingWrites;
	protected ScheduledExecutorService cancelExecutor;
	
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


	public ExecutorService getWriteExecutor(){
		return writeExecutor;
	}


	public Queue<OutstandingWriteWrapper> getOutstandingWrites(){
		return outstandingWrites;
	}


	public N getBackingNode(){
		return backingNode;
	}
	
	
}
