package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class ManualMultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		N extends MapStorageNode<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueKeyIndexEntry<IK,PK,D>,
		IN extends SortedMapStorageNode<IK,IE>>
implements IndexedStorage<PK,D>{

	protected N mainNode;
	protected IN indexNode;
	
	public ManualMultiIndexNode(N mainNode, IN indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	//TODO should i be passing config options around blindly?
	
	
	/********************* IndexedStorageReader ******************************/
	
	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey==null){ return null; }
		IE indexEntryParam = (IE)uniqueKey;
		IE indexEntry = indexNode.get(indexEntryParam.getKey(), config);
		if(indexEntry==null){ return null; }
		PK primaryKey = indexEntry.getTargetKey();
		return mainNode.get(primaryKey, config);
	}

//	@Override
//	public D lookupUnique(IK indexKey, Config config){
//		if(indexKey==null){ return null; }
//		IE indexEntry = indexNode.get(indexKey, config);
//		if(indexEntry==null){ return null; }
//		PK primaryKey = indexEntry.getTargetKey();
//		return mainNode.get(primaryKey, config);
//	}
	
	
	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		List<IK> indexPrimaryKeys = ListTool.createArrayListWithSize(uniqueKeys);
		for(UniqueKey<PK> uniqueKey : uniqueKeys){
			IE indexEntryParam = (IE)uniqueKey;
			indexPrimaryKeys.add(indexEntryParam.getKey());
		}
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
	
	
	/********************* IndexedStorageWriter ******************************/
	
	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		if(uniqueKey==null){ return; }
		IE indexEntryParam = (IE)uniqueKey;
		IE indexEntry = indexNode.get(indexEntryParam.getKey(), config);
		if(indexEntry==null){ return; }
		PK primaryKey = indexEntry.getTargetKey();
		mainNode.delete(primaryKey, config);
		indexNode.delete(indexEntry.getKey(), config);
	}
	
	
	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return; }
		List<IK> indexPrimaryKeys = ListTool.createArrayListWithSize(uniqueKeys);
		for(UniqueKey<PK> uniqueKey : uniqueKeys){
			IE indexEntryParam = (IE)uniqueKey;
			indexPrimaryKeys.add(indexEntryParam.getKey());
		}
		List<IE> indexEntries = indexNode.getMulti(indexPrimaryKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		mainNode.deleteMulti(primaryKeys, config);
		indexNode.deleteMulti(KeyTool.getKeys(indexEntries), config);
	}

	
	@Override
	public void delete(Lookup<PK> lookup, Config config) {
		throw new NotImplementedException("only unique indexes currently supported");
	}
	
}
