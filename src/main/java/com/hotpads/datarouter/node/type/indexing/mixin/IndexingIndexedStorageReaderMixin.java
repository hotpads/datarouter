package com.hotpads.datarouter.node.type.indexing.mixin;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IndexingIndexedStorageReaderMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueKeyIndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>,
		IN extends SortedMapStorageNode<IK,IE>>
implements IndexedStorageReader<PK,D>{

	protected N mainNode;
	protected Map<Class<IE>,IN> indexNodeByClass;
	
	public IndexingIndexedStorageReaderMixin(N mainNode, List<? extends IN> indexNodes){
		this.mainNode = mainNode;
		this.indexNodeByClass = new HashMap<>();
		for(IN indexNode : DrIterableTool.nullSafe(indexNodes)){
			indexNodeByClass.put(indexNode.getDatabeanType(), indexNode);
		}
	}

	@Override
	public Long count(Lookup<PK> lookup, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
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
		if(DrCollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		List<IK> indexPrimaryKeys = DrListTool.createArrayListWithSize(uniqueKeys);
		for(UniqueKey<PK> uniqueKey : uniqueKeys){
			@SuppressWarnings("unchecked")
			IK indexPrimaryKey = (IK)uniqueKey;
			indexPrimaryKeys.add(indexPrimaryKey);
		}
		IN indexNode = indexNodeByClass.get(DrCollectionTool.getFirst(indexPrimaryKeys).getClass());
		if(indexNode==null){ throw new IllegalArgumentException("no index found for type="+DrCollectionTool.getFirst(indexPrimaryKeys)); }
		List<IE> indexEntries = indexNode.getMulti(indexPrimaryKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}

	
	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
	}
	
	
	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
	}
	
}
