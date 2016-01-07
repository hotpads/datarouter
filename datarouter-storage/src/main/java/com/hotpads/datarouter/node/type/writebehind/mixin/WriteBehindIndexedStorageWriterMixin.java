package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class WriteBehindIndexedStorageWriterMixin<
	PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	N extends IndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{

	private BaseWriteBehindNode<PK,D,N> node;

	public WriteBehindIndexedStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node) {
		this.node = node;
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		node.getQueue().offer(new WriteWrapper<>(OP_indexDelete, DrListTool.wrap(lookup), config));
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		node.getQueue().offer(new WriteWrapper<>(OP_deleteUnique, DrListTool.wrap(uniqueKey), config));
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		node.getQueue().offer(new WriteWrapper<>(OP_deleteUnique, uniqueKeys, config));
	}
	
	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		node.getQueue().offer(new WriteWrapper<>(OP_deleteByIndex, keys, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		//very weird that this class is calling methods by their OP name.
		throw new NotImplementedException();
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		//very weird that this class is calling methods by their OP name.
		throw new NotImplementedException();
	}

}
