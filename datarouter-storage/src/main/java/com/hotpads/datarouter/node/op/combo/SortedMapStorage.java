package com.hotpads.datarouter.node.op.combo;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.node.op.combo.writer.SortedMapStorageWriter;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.BatchingIterable;

public interface SortedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorage<PK,D>, SortedStorage<PK,D>, SortedMapStorageReader<PK,D>, SortedMapStorageWriter<PK,D>{

	static final int DELETE_BATCH_SIZE = 100;

	default void deleteWithPrefix(PK prefix, Config config){
		for(List<PK> keys : new BatchingIterable<>(scanKeysWithPrefix(prefix, config), DELETE_BATCH_SIZE)){
			deleteMulti(keys, config);
		}
	}

	public interface SortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedMapStorage<PK,D>, MapStorageNode<PK,D>, SortedStorageNode<PK,D>, SortedMapStorageReaderNode<PK,D>,
			SortedMapStorageWriterNode<PK,D>{
	}
	public interface PhysicalSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedMapStorageNode<PK,D>, PhysicalMapStorageNode<PK,D>, PhysicalSortedStorageNode<PK,D>,
			PhysicalSortedMapStorageReaderNode<PK,D>, PhysicalSortedMapStorageWriterNode<PK,D>{
	}
}
