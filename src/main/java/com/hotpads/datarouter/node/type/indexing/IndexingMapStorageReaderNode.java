package com.hotpads.datarouter.node.type.indexing;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.indexing.base.BaseIndexingNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class IndexingMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseIndexingNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{
	
	public IndexingMapStorageReaderNode(N mainNode) {
		super(mainNode);//mainNode must have explicit Fielder
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		return mainNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
		return mainNode.get(key, config);
	}

	@Override
	public List<D> getAll(Config config) {
		return mainNode.getAll(config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		return mainNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		return mainNode.getKeys(keys, config);
	}
	
}