package com.hotpads.datarouter.node.factory;

import java.util.Collection;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.redundant.RedundantSortedMapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class RedundantNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	RedundantSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>> newSortedMap(Class<D> databeanClass, 
			Datarouter router, 
			Collection<SortedMapStorageNode<PK,D>> writeNodes, 
			SortedMapStorageNode<PK,D> readNode){
		
		return new RedundantSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>>(
				databeanClass, router, writeNodes, readNode);
	}
}









