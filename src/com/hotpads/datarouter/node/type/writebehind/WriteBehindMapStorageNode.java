package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.writebehind.OutstandingWriteWrapper;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public class WriteBehindMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements MapStorageNode<PK,D>{
	
	public WriteBehindMapStorageNode(Class<D> databeanClass, DataRouter router,
			N backingNode, ExecutorService writeExecutor, ScheduledExecutorService cancelExecutor) {
		super(databeanClass, router, backingNode, writeExecutor, cancelExecutor);
	}
	
	
	/***************************** MapStorageWriter ****************************/

	@Override
	public void delete(final PK key, final Config config) {
		outstandingWrites.add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.delete(key, config);
				}catch(Exception e){
					logger.error("error on delete["+key.toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteAll(final Config config) {
		outstandingWrites.add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.deleteAll(config);
				}catch(Exception e){
					logger.error("error on deleteAll");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config) {
		outstandingWrites.add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.deleteMulti(keys, config);
				}catch(Exception e){
					logger.error("error on deleteMulti including["+CollectionTool.getFirst(keys)+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void put(final D databean, final Config config) {
		outstandingWrites.add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.put(databean, config);
				}catch(Exception e){
					logger.error("error on put["+databean.getKey().toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void putMulti(final Collection<D> databeans, final Config config) {
		outstandingWrites.add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				writeExecutor.submit(new Callable<Void>(){
			public Void call(){
				try{
					backingNode.putMulti(databeans, config);
				}catch(Exception e){
					logger.error("error on putMulti");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}
	
	
	
}
