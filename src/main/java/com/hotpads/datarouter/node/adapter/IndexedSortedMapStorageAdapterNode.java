package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;

public class IndexedSortedMapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends IndexedSortedMapStorageReaderAdapterNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>{
	
	public IndexedSortedMapStorageAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
	}
	

	/***************** MapStorage ************************************/
	//TODO these are copy/pasted from MapStorageAdapterNode.  use a mixin if they get more complex
	
	@Override
	public void put(D databean, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_put, 1));
		backingNode.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		int numItems = CollectionTool.size(databeans);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_putMulti, numItems));
		backingNode.putMulti(databeans, config);
	}

	@Override
	public void delete(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_delete, 1));
		backingNode.delete(key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		int numItems = CollectionTool.size(keys);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteMulti, numItems));
		backingNode.deleteMulti(keys, config);
	}

	@Override
	public void deleteAll(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteAll, 0));
		backingNode.deleteAll(config);
	}
	

	/***************** SortedMapStorage ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageWriter.OP_deleteRangeWithPrefix, 1));
		backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	

	/***************** IndexedSortedMapStorage ************************************/

	@Override
	public void delete(Lookup<PK> lookup, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageWriter.OP_indexDelete, 1));
		backingNode.delete(lookup, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageWriter.OP_deleteUnique, 1));
		backingNode.deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		int numItems = CollectionTool.size(uniqueKeys);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageWriter.OP_deleteMultiUnique, numItems));
		backingNode.deleteMultiUnique(uniqueKeys, config);
	}

	
}
