package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.OutstandingWriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.ExceptionTool;

public class WriteBehindIndexedStorageWriterMixin<
	PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	N extends IndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{
	private Logger logger = Logger.getLogger(getClass());

	protected BaseWriteBehindNode<PK,D,N> node;

	public WriteBehindIndexedStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node) {
		this.node = node;
	}

	@Override
	public void delete(final Lookup<PK> lookup, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().delete(lookup, config);
				}catch(Exception e){
					logger.error("error on delete["+lookup+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteUnique(final UniqueKey<PK> uniqueKey, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteUnique(uniqueKey, config);
				}catch(Exception e){
					logger.error("error on deleteUnique["+uniqueKey+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteMultiUnique(uniqueKeys, config);
				}catch(Exception e){
					logger.error("error on deleteMultiUnique");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

}
