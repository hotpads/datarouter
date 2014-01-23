package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.node.op.index.UniqueIndexWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.CollectionTool;

public class ManualUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>>
implements UniqueIndexReader<PK,D,IK>,UniqueIndexWriter<PK,D,IK>{

	protected CompoundMapRWStorage<PK,D> mainNode;
	protected SortedMapStorage<IK,IE> indexNode;
	
	public ManualUniqueIndexNode(CompoundMapRWStorage<PK,D> mainNode, SortedMapStorage<IK,IE> indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	//TODO should i be passing config options around blindly?
	
		
	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		if(uniqueKey==null){ return null; }
		IE indexEntry = indexNode.get(uniqueKey, config);
		if(indexEntry==null){ return null; }
		PK primaryKey = indexEntry.getTargetKey();
		D databean = mainNode.reader().get(primaryKey, config);
		return databean;
	}
	
	
	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		List<IE> indexEntries = indexNode.getMulti(uniqueKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.reader().getMulti(primaryKeys, config);
		return databeans;
	}

	
	@Override
	public void deleteUnique(IK indexKey, Config config){
		if(indexKey==null){ return; }
		IE indexEntry = indexNode.get(indexKey, config);
		if(indexEntry==null){ return; }
		PK primaryKey = indexEntry.getTargetKey();
		indexNode.delete(indexKey, config);
		mainNode.writer().delete(primaryKey, config);
	}
	
	
	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return; }
		List<IE> indexEntries = indexNode.getMulti(uniqueKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		indexNode.deleteMulti(uniqueKeys, config);
		mainNode.writer().deleteMulti(primaryKeys, config);
	}
	
}
