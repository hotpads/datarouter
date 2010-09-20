package com.hotpads.datarouter.node.base.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.profile.count.collection.Counters;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseWriteBehindNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D>
implements MapStorageReadOps<PK,D>{
	
	public static final int DEFAULT_WRITE_BEHIND_THREADS = 1;
	public static final long DEFAULT_TIMEOUT_MS = 60*1000;
	
	protected N backingNode;
	protected ExecutorService writeExecutor;
	protected long timeoutMs;//TODO also limit by queue length
	protected Queue<Future<?>> outstandingWrites;
	protected Queue<Long> outstandingWriteStartTimes;
	protected ScheduledExecutorService cancelExecutor;
	
	public BaseWriteBehindNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass);
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
		this.outstandingWrites = new ConcurrentLinkedQueue<Future<?>>();
		this.outstandingWriteStartTimes = new ConcurrentLinkedQueue<Long>();
		
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
	
	
	protected static class OverdueWriteCanceller implements Runnable{
		static Logger logger = Logger.getLogger(CountArchiveFlusher.class);
		
		protected final BaseWriteBehindNode<?,?,?> node;
		
		public OverdueWriteCanceller(BaseWriteBehindNode<?,?,?> node){
			this.node = node;
		}

		@Override
		public void run(){
			try{
				if(node.outstandingWrites==null){ return; }
				while(true){
					Future<?> outstandingWrite = node.outstandingWrites.peek();//don't remove yet
					Long writeStartMs = node.outstandingWriteStartTimes.peek();
					if(ObjectTool.bothNull(outstandingWrite, writeStartMs)){ break; }
					if(ObjectTool.isOneNullButNotTheOther(outstandingWrite, writeStartMs)){//fix out of sync queues
						if(outstandingWrite==null){ node.outstandingWrites.poll(); }
						if(writeStartMs==null){ node.outstandingWriteStartTimes.poll(); }
					}
					long elapsedMs = System.currentTimeMillis() - writeStartMs;
					boolean overdue = elapsedMs > node.timeoutMs;
					if(outstandingWrite.isDone() || overdue){ 
						if(overdue){
							logger.warn("cancelling overdue write on "+node.name);
							Counters.inc("writeBehind timeout on "+node.name);
						}
						node.outstandingWrites.poll(); 
						node.outstandingWriteStartTimes.poll();
						continue;
					}
					break;//wait to be triggered again
				}
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
	}
	
	

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		names.addAll(CollectionTool.nullSafe(name));
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
}
