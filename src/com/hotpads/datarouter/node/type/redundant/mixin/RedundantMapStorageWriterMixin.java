package com.hotpads.datarouter.node.type.redundant.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.redundant.BaseRedundantNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	protected BaseRedundantNode<PK,D,N> target;
	
	public RedundantMapStorageWriterMixin(BaseRedundantNode<PK,D,N> target){
		this.target = target;
	}

	@Override
	public void delete(PK key, Config config) {
		for(N n : target.getWriteNodes()){
			n.delete(key, config);
		}
	}

	@Override
	public void deleteAll(Config config) {
		for(N n : target.getWriteNodes()){
			n.deleteAll(config);
		}
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(N n : target.getWriteNodes()){
			n.deleteMulti(keys, config);
		}
	}

	@Override
	public void put(D databean, Config config) {
		for(N n : target.getWriteNodes()){
			n.put(databean, config);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(N n : target.getWriteNodes()){
			n.putMulti(databeans, config);
		}
	}
	
}
