package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.index.BaseIndexNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

/*
 * this assumes that only PK fields are changed... it has no way of detecting, for example, if User.email changes
 * 
 * originally written for ModelIndexListingView, where the feedId_feedListingId index is known from the 
 *  PK (quad_feedId_feedListingId)... perhaps a rare case, but much easier to implement
 */
public class IndexMapStorageWriterListener<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueKeyIndexEntry<IK,PK,D>,
		IN extends SortedMapStorageNode<IK,IE>>
extends BaseIndexNode<PK,D,IK,IE,IN>
implements IndexListener<PK,D>{
	
	public IndexMapStorageWriterListener(Class<IE> indexEntryClass, IN indexNode){
		super(indexEntryClass, indexNode);
	}
	
	//TODO is passing the configs straight through the right thing to do?
	
	/********************** writing ******************************/
	
	@Override
	public void onDelete(PK key, Config config) {
		IE indexEntry = createIndexEntry();
		indexEntry.fromPrimaryKey(key);
		indexNode.delete(indexEntry.getKey(), config);
	}

	@Override
	public void onDeleteAll(Config config) {
		indexNode.deleteAll(config);
	}

	@Override
	public void onDeleteMulti(Collection<PK> keys, Config config) {
		List<IE> indexEntries = getIndexEntries(keys);
		indexNode.deleteMulti(KeyTool.getKeys(indexEntries), config);
	}

	@Override
	public void onPut(D databean, Config config) {
		IE indexEntry = createIndexEntry();
		indexEntry.fromPrimaryKey(databean.getKey());
		indexNode.put(indexEntry, config);
	}

	@Override
	public void onPutMulti(Collection<D> databeans, Config config) {
		List<IE> indexEntries = getIndexEntries(KeyTool.getKeys(databeans));
		indexNode.putMulti(indexEntries, config);
	}
	

	/******************* helper **************************/
	
	protected List<IE> getIndexEntries(Collection<PK> primaryKeys){
		List<IE> indexEntries = ListTool.createArrayListWithSize(primaryKeys);
		for(PK key : IterableTool.nullSafe(primaryKeys)){
			IE indexEntry = createIndexEntry();
			indexEntry.fromPrimaryKey(key);
			indexEntries.add(indexEntry);
		}
		return indexEntries;
	}
	
}
