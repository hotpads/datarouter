package com.hotpads.datarouter.node.type.indexing.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;

public class IndexingMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{

	protected N mainNode;
	protected List<IndexListener<PK,D>> indexNodes;
	
	public IndexingMapStorageWriterMixin(N mainNode, 
			List<IndexListener<PK,D>> indexNodes){
		this.mainNode = mainNode;
		this.indexNodes = ListTool.nullSafe(indexNodes);
	}

	@Override
	public void delete(PK key, Config config){
		for(IndexListener<PK,D> indexNode : indexNodes){
			indexNode.onDelete(key, config);
		}
		mainNode.delete(key, config);
	}

	@Override
	public void deleteAll(Config config) {
		for(IndexListener<PK,D> indexNode : indexNodes){
			indexNode.onDeleteAll(config);
		}
		mainNode.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(IndexListener<PK,D> indexNode : indexNodes){
			indexNode.onDeleteMulti(keys, config);
		}
		mainNode.deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config) {
		for(IndexListener<PK,D> indexNode : indexNodes){
			indexNode.onPut(databean, config);
		}
		mainNode.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(IndexListener<PK,D> indexNode : indexNodes){
			indexNode.onPutMulti(databeans, config);
		}
		mainNode.putMulti(databeans, config);
	}

	
	
}
