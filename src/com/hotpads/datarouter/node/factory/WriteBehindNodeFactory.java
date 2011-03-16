package com.hotpads.datarouter.node.factory;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class WriteBehindNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	WriteBehindSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>> newSortedMap(Class<D> databeanClass, 
			DataRouter router, 
			SortedMapStorageNode<PK,D> backingNode){
		
		return new WriteBehindSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>>(
				databeanClass, router, backingNode, null, null);
	}
}









