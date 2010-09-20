package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public class WriteBehindSortedStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends SortedStorageNode<PK,D>>
extends WriteBehindSortedStorageReaderNode<PK,D,N>
implements SortedStorageNode<PK,D>{
	
	public WriteBehindSortedStorageNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
	}
	
	
	/********************** sorted storage write ops ************************/

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, config);
				}catch(Exception e){
					logger.error("error on deleteRangeWithPrefix["+prefix.toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}
	

	/***************************** MapStorageWriter ****************************/

	/*
	 * MULTIPLE INHERITANCE... copied from NonBlockingWriteMapStorageNode
	 */


	@Override
	public void delete(final PK key, final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.delete(key, config);
				}catch(Exception e){
					logger.error("error on delete["+key.toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}

	@Override
	public void deleteAll(final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.deleteAll(config);
				}catch(Exception e){
					logger.error("error on deleteAll");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.deleteMulti(keys, config);
				}catch(Exception e){
					logger.error("error on deleteMulti including["+CollectionTool.getFirst(keys)+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}

	@Override
	public void put(final D databean, final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.put(databean, config);
				}catch(Exception e){
					logger.error("error on put["+databean.getKey().toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}

	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		outstandingWriteStartTimes.add(System.currentTimeMillis());
		outstandingWrites.add(writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.putMulti(databeans, config);
				}catch(Exception e){
					logger.error("error on putMulti");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		}));
	}
	
	
	
}
