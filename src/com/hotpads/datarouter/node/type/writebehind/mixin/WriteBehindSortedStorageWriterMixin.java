package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.writebehind.BaseWriteBehindNode;
import com.hotpads.datarouter.node.base.writebehind.OutstandingWriteWrapper;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.SortedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ExceptionTool;

public class WriteBehindSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends SortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{
	Logger logger = Logger.getLogger(getClass());

	protected BaseWriteBehindNode<PK,D,N> node;
	
	public WriteBehindSortedStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
	}
	
	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteRangeWithPrefix(prefix, wildcardLastField, config);
				}catch(Exception e){
					logger.error("error on deleteRangeWithPrefix["+prefix.toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}
}
