package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrListTool;

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
		node.getQueue().offer(new WriteWrapper<Lookup<PK>>(OP_indexDelete, DrListTool.wrap(lookup), config));
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		node.getQueue().offer(new WriteWrapper<UniqueKey<PK>>(OP_deleteUnique, DrListTool.wrap(uniqueKey), config));
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		node.getQueue().offer(new WriteWrapper<>(OP_deleteUnique, uniqueKeys, config));
	}

}
