package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.index.base.BaseIndexNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.storage.view.index.KeyIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

/*
 * this assumes that only PK fields are changed... it has no way of detecting, for example, if User.email changes
 * 
 * originally written for ModelIndexListingView, where the feedId_feedListingId index is known from the 
 *  PK (quad_feedId_feedListingId)... perhaps a rare case, but much easier to implement
 *  
 * also fine for cases where you never delete or modify records, like the Event table
 * 
 */
public class IndexMapStorageWriterListener<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IN extends SortedMapStorageNode<IK,IE>>
extends BaseIndexNode<PK,D,IK,IE,IN>
implements IndexListener<PK,D>{
	
	public IndexMapStorageWriterListener(Class<IE> indexEntryClass, IN indexNode){
		super(indexEntryClass, indexNode);//indexNode must have explicit Fielder
	}
	
	//TODO is passing the configs straight through the right thing to do?
	
	/********************** writing ******************************/
	
	@SuppressWarnings("unchecked")
	@Override
	public void onDelete(PK key, Config config) {
		IE indexEntry = createIndexEntry();
		if(indexEntry instanceof KeyIndexEntry){
			((KeyIndexEntry)indexEntry).fromPrimaryKey(key);
			indexNode.delete(indexEntry.getKey(), config);
		}
		else{
			//there is currently no way to delete an index entry whose PK can't be calculated from the target PK.
			// options:
			//  1) read the databean before deleting
			//  2) user provides the indexEntry key (maybe he read the whole databean moments earlier)
			//  3) don't delete it and vacuum the whole table later
		}
	}

	@Override
	public void onDeleteAll(Config config) {
		indexNode.deleteAll(config);
	}

	@Override
	public void onDeleteMulti(Collection<PK> keys, Config config) {
		List<IE> indexEntries = getIndexEntriesFromPrimaryKeys(keys);
		indexNode.deleteMulti(KeyTool.getKeys(indexEntries), config);
	}

	@Override
	public void onPut(D databean, Config config) {
		IE sampleIndexEntry = createIndexEntry();
		List<IE> indexEntries = sampleIndexEntry.createFromDatabean(databean);
		indexNode.putMulti(indexEntries, config);
//		indexEntry.fromDatabean(databean);
//		indexNode.put(indexEntry, config);
	}

	@Override
	public void onPutMulti(Collection<D> databeans, Config config) {
		List<IE> indexEntries = getIndexEntriesFromDatabeans(databeans);
		indexNode.putMulti(indexEntries, config);
	}
	

	/******************* helper **************************/
	
	@SuppressWarnings("unchecked")
	protected List<IE> getIndexEntriesFromPrimaryKeys(Collection<PK> primaryKeys){
		List<IE> indexEntries = DrListTool.createArrayListWithSize(primaryKeys);
		for(PK key : DrIterableTool.nullSafe(primaryKeys)){
			if(key == null){
				throw new IllegalArgumentException("invalid null key") ;
			}
			IE indexEntry = createIndexEntry();
			if(indexEntry instanceof UniqueKeyIndexEntry){
				((UniqueKeyIndexEntry)indexEntry).fromPrimaryKey(key);
				indexEntries.add(indexEntry);
			}else{
				//we don't know enough.  don't return anything
			}
		}
		return indexEntries;
	}
	
	protected List<IE> getIndexEntriesFromDatabeans(Collection<D> databeans){
		IE sampleIndexEntry = createIndexEntry();
		List<IE> indexEntries = DrListTool.createArrayListWithSize(databeans);
		for(D databean : DrIterableTool.nullSafe(databeans)){
			if(databean == null){
				throw new IllegalArgumentException("invalid null databean") ;
			}
			List<IE> indexEntriesFromSingleDatabean = sampleIndexEntry.createFromDatabean(databean);
			indexEntries.addAll(DrCollectionTool.nullSafe(indexEntriesFromSingleDatabean));
		}
		return indexEntries;
	}
	
}
