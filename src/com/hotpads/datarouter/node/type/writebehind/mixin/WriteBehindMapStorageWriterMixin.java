package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.OutstandingWriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

/*
 * currently implements queuing functionality by wrapping each call into a "Callable" and 
 *  putting it into the ExecutorService's queue.  
 *  
 * a more efficient version might batch writes together, requiring a customized queuing system
 */
public class WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
implements MapStorageWriter<PK,D>{
	Logger logger = Logger.getLogger(getClass());
	
	protected BaseWriteBehindNode<PK,D,N> node;
	
	public WriteBehindMapStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
	}

	@Override
	public void delete(final PK key, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().delete(key, config);
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
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteAll(config);
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
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteMulti(keys, config);
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
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().put(databean, config);
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
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().putMulti(databeans, config);
				}catch(Exception e){
					logger.error("error on putMulti");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}
	

	
	
}
