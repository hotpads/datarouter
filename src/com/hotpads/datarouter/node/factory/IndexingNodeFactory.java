package com.hotpads.datarouter.node.factory;

import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.index.IndexMapStorageWriterListener;
import com.hotpads.datarouter.node.type.index.ManualUniqueIndexNode;
import com.hotpads.datarouter.node.type.indexing.IndexingMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.key.UniqueIndexEntry;
import com.hotpads.datarouter.storage.view.index.key.UniqueKeyIndexEntry;

public class IndexingNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>>
	IndexingMapStorageNode<PK,D,MapStorageNode<PK,D>> 
	newMap(MapStorageNode<PK,D> mainNode){
		
		IndexingMapStorageNode<PK,D,MapStorageNode<PK,D>> result = 
			new IndexingMapStorageNode<PK,D,MapStorageNode<PK,D>>(mainNode);
		return result;
		
	}
	
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueKeyIndexEntry<IK,PK,D>,
					IN extends SortedMapStorageNode<IK,IE>>
	IndexListener<PK,D> newListener(Class<IE> indexEntryClass, IN indexNode){
		return new IndexMapStorageWriterListener<PK,D,IK,IE,SortedMapStorageNode<IK,IE>>(
				indexEntryClass, indexNode);
	}
	
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,PK,D>>
	ManualUniqueIndexNode<PK,D,IK,IE> newManualUnique(CompoundMapRWStorage<PK,D> mainNode, 
			SortedMapStorageNode<IK,IE> indexNode){
		return new ManualUniqueIndexNode<PK,D,IK,IE>(mainNode, indexNode);
	}
}









