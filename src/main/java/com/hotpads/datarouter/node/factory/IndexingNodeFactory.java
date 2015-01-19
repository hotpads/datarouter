package com.hotpads.datarouter.node.factory;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.index.IndexMapStorageWriterListener;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.index.ManualMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManualUniqueIndexNode;
import com.hotpads.datarouter.node.type.indexing.IndexingMapStorageNode;
import com.hotpads.datarouter.node.type.indexing.IndexingSortedMapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;

public class IndexingNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	IndexingMapStorageNode<PK,D,DatabeanFielder<PK,D>,MapStorageNode<PK,D>> 
	newMap(MapStorageNode<PK,D> mainNode){
		
		IndexingMapStorageNode<PK,D,DatabeanFielder<PK,D>,MapStorageNode<PK,D>> result = 
			new IndexingMapStorageNode<PK,D,DatabeanFielder<PK,D>,MapStorageNode<PK,D>>(mainNode);
		return result;
		
	}
	
	public static <PK extends PrimaryKey<PK>, 
					D extends Databean<PK, D>, 
					F extends DatabeanFielder<PK, D>, 
					N extends SortedMapStorageNode<PK, D>>
	IndexingSortedMapStorageNode<PK, D, F, N> newSortedMap(N mainNode){
		return new IndexingSortedMapStorageNode<PK, D, F, N>(mainNode);
	}
	
	/************************** listener *****************************************/
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueKeyIndexEntry<IK,IE,PK,D>,
					IN extends SortedMapStorageNode<IK,IE>>
	IndexListener<PK,D> newUniqueKeyListener(Class<IE> indexEntryClass, IN indexNode){
		return new IndexMapStorageWriterListener<PK,D,IK,IE,SortedMapStorageNode<IK,IE>>(
				indexEntryClass, indexNode);
	}
	
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IN extends SortedMapStorageNode<IK,IE>>
	IndexListener<PK,D> newMultiListener(Class<IE> indexEntryClass, IN indexNode){
		return new IndexMapStorageWriterListener<PK,D,IK,IE,SortedMapStorageNode<IK,IE>>(
				indexEntryClass, indexNode);//indexNode must have explicit Fielder
	}
	
	
	/******************************* indexing node **************************************/	
	
	/**** Manual indexes ****/
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>>
	ManualUniqueIndexNode<PK,D,IK,IE> newManualUnique(CompoundMapRWStorage<PK,D> mainNode, 
			SortedMapStorageNode<IK,IE> indexNode){
		return new ManualUniqueIndexNode<PK,D,IK,IE>(mainNode, indexNode);
	}
	
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>>
	ManualMultiIndexNode<PK,D,IK,IE> newManualMulti(CompoundMapRWStorage<PK,D> mainNode, 
			SortedMapStorageNode<IK,IE> indexNode){
		return new ManualMultiIndexNode<PK,D,IK,IE>(mainNode, indexNode);
	}

	/**** Managed indexes ****/
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE, IF> newManagedUnique(Datarouter router, PhysicalMapStorageNode<PK, D> backingNode, 
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn, String indexName){
		NodeParams<IK, IE, IF> params = new NodeParamsBuilder<IK, IE, IF>(router, indexEntryClass).withFielder(
				indexFielder).build();
		return router.getClientType(backingNode.getClientName()).createManagedUniqueIndexNode(backingNode,
				params, indexName, manageTxn);
	}
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedMultiIndexNode<PK, D, IK, IE, IF> newManagedMulti(Datarouter router, PhysicalMapStorageNode<PK, D> backingNode, 
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn, String indexName){
		NodeParams<IK, IE, IF> params = new NodeParamsBuilder<IK, IE, IF>(router, indexEntryClass).withFielder(
				indexFielder).build();
		return router.getClientType(backingNode.getClientName()).createManagedMultiIndexNode(backingNode,
				params, indexName, manageTxn);
	}
	
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE, IF> newManagedUnique(Datarouter router, PhysicalMapStorageNode<PK, D> backingNode, 
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn){
		return newManagedUnique(router, backingNode, indexFielder, indexEntryClass, manageTxn,
				indexEntryClass.getSimpleName());
	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedMultiIndexNode<PK, D, IK, IE, IF> newManagedMulti(Datarouter router, PhysicalMapStorageNode<PK, D> backingNode, 
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn){
		return newManagedMulti(router, backingNode, indexFielder, indexEntryClass, manageTxn,
			indexEntryClass.getSimpleName());
	}
}
