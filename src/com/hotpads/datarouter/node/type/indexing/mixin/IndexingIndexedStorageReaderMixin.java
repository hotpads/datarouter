package com.hotpads.datarouter.node.type.indexing.mixin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IndexingIndexedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueKeyIndexEntry<IK,IE,PK,D>,
		IN extends SortedMapStorageNode<IK,IE>>
implements IndexedStorageReader<PK,D>{

	protected N mainNode;
	protected Map<Class<IE>,IN> indexNodeByClass;
	
	public IndexingIndexedStorageReaderMixin(N mainNode, List<? extends IN> indexNodes){
		this.mainNode = mainNode;
		this.indexNodeByClass = MapTool.createHashMap();
		for(IN indexNode : IterableTool.nullSafe(indexNodes)){
			indexNodeByClass.put(indexNode.getDatabeanType(), indexNode);
		}
	}

	
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey==null){ return null; }
		@SuppressWarnings("unchecked")
		IK indexPrimaryKey = (IK)uniqueKey;
		IN indexNode = indexNodeByClass.get(indexPrimaryKey.getClass());
		if(indexNode==null){ throw new IllegalArgumentException("no index found for type="+indexPrimaryKey); }
		IE indexEntry = indexNode.get(indexPrimaryKey, config);
		if(indexEntry==null){ return null; }
		PK primaryKey = indexEntry.getTargetKey();
		return mainNode.get(primaryKey, config);
	}
	
	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		List<IK> indexPrimaryKeys = ListTool.createArrayListWithSize(uniqueKeys);
		for(UniqueKey<PK> uniqueKey : uniqueKeys){
			@SuppressWarnings("unchecked")
			IK indexPrimaryKey = (IK)uniqueKey;
			indexPrimaryKeys.add(indexPrimaryKey);
		}
		IN indexNode = indexNodeByClass.get(CollectionTool.getFirst(indexPrimaryKeys).getClass());
		if(indexNode==null){ throw new IllegalArgumentException("no index found for type="+CollectionTool.getFirst(indexPrimaryKeys)); }
		List<IE> indexEntries = indexNode.getMulti(indexPrimaryKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
	}
	
}
