package com.hotpads.datarouter.node.factory;

import java.util.function.Supplier;

import com.hotpads.datarouter.client.imp.jdbc.node.index.NoTxnManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.NoTxnManagedUniqueIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.TxnManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.TxnManagedUniqueIndexNode;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.index.IndexMapStorageWriterListener;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.index.ManualMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManualUniqueIndexNode;
import com.hotpads.datarouter.node.type.indexing.IndexingMapStorageNode;
import com.hotpads.datarouter.node.type.indexing.IndexingSortedMapStorageNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.FieldlessIndexEntry;
import com.hotpads.datarouter.storage.field.FieldlessIndexEntryFielder;
import com.hotpads.datarouter.storage.key.FieldlessIndexEntryPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueKeyIndexEntry;
import com.hotpads.util.core.java.ReflectionTool;

public class IndexingNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	IndexingMapStorageNode<PK,D,DatabeanFielder<PK,D>,MapStorageNode<PK,D>>
	newMap(MapStorageNode<PK,D> mainNode){

		IndexingMapStorageNode<PK,D,DatabeanFielder<PK,D>,MapStorageNode<PK,D>> result =
			new IndexingMapStorageNode<>(mainNode);
		return result;

	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK, D>,
					F extends DatabeanFielder<PK, D>,
					N extends SortedMapStorageNode<PK, D>>
	IndexingSortedMapStorageNode<PK, D, F, N> newSortedMap(N mainNode){
		return new IndexingSortedMapStorageNode<>(mainNode);
	}

	/************************** listener *****************************************/

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueKeyIndexEntry<IK,IE,PK,D>,
					IN extends SortedMapStorageNode<IK,IE>>
	IndexListener<PK,D> newUniqueKeyListener(Class<IE> indexEntryClass, IN indexNode){
		return new IndexMapStorageWriterListener<PK,D,IK,IE,SortedMapStorageNode<IK,IE>>(
				ReflectionTool.supplier(indexEntryClass), indexNode);
	}


	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IN extends SortedMapStorageNode<IK,IE>>
	IndexListener<PK,D> newMultiListener(Class<IE> indexEntryClass, IN indexNode){
		return new IndexMapStorageWriterListener<PK,D,IK,IE,SortedMapStorageNode<IK,IE>>(
				ReflectionTool.supplier(indexEntryClass), indexNode);//indexNode must have explicit Fielder
	}


	/******************************* indexing node **************************************/

	/**** Manual indexes ****/
	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>>
	ManualUniqueIndexNode<PK,D,IK,IE> newManualUnique(MapStorage<PK,D> mainNode,
			SortedMapStorageNode<IK,IE> indexNode){
		return new ManualUniqueIndexNode<>(mainNode, indexNode);
	}


	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>>
	ManualMultiIndexNode<PK,D,IK,IE> newManualMulti(MapStorage<PK,D> mainNode,
			SortedMapStorageNode<IK,IE> indexNode){
		return new ManualMultiIndexNode<>(mainNode, indexNode);
	}

	/**** Managed indexes ****/
	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	ManagedUniqueIndexNode<PK,D,IK,FieldlessIndexEntry<IK,PK,D>,FieldlessIndexEntryFielder<IK,PK,D>>
			newKeyOnlyManagedUnique(Router router, IndexedMapStorage<PK, D> backingNode, boolean manageTxn,
					String indexName, Class<IK> indexKeyClass){
		return newManagedUnique(router, backingNode, () -> new FieldlessIndexEntryFielder<>(indexKeyClass),
				() -> new FieldlessIndexEntry<>(indexKeyClass), manageTxn, indexName);
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	ManagedUniqueIndexNode<PK,D,IK,FieldlessIndexEntry<IK,PK,D>,FieldlessIndexEntryFielder<IK,PK,D>>
			newKeyOnlyManagedUnique(Router router, IndexedMapStorage<PK, D> backingNode, boolean manageTxn,
					Class<IK> indexKeyClass){
		return newKeyOnlyManagedUnique(router, backingNode, manageTxn, indexKeyClass.getSimpleName(), indexKeyClass);
	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE, IF> newManagedUnique(Router router,
			IndexedMapStorage<PK, D> backingNode, Supplier<IF> indexFielderSupplier, Supplier<IE> indexEntrySupplier,
			boolean manageTxn, String indexName){
		NodeParams<IK, IE, IF> params = new NodeParamsBuilder<IK, IE, IF>(router, indexEntrySupplier)
				.withFielder(indexFielderSupplier).build();
		if(manageTxn){
			return new TxnManagedUniqueIndexNode<>(backingNode, params, indexName);
		}
		return new NoTxnManagedUniqueIndexNode<>(backingNode, params, indexName);
	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends UniqueIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE, IF> newManagedUnique(Router router, IndexedMapStorage<PK, D> backingNode,
			Class<IF> indexFielder, Class<IE> indexEntryClass, boolean manageTxn){
		return newManagedUnique(router, backingNode, ReflectionTool.supplier(indexFielder), ReflectionTool.supplier(
				indexEntryClass), manageTxn, indexEntryClass.getSimpleName());
	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedMultiIndexNode<PK, D, IK, IE, IF> newManagedMulti(Router router,
			IndexedMapStorage<PK, D> backingNode, Class<IF> indexFielder, Class<IE> indexEntryClass,
			boolean manageTxn, String indexName){
		NodeParams<IK, IE, IF> params = new NodeParamsBuilder<IK, IE, IF>(router, ReflectionTool.supplier(
				indexEntryClass)).withFielder(indexFielder).build();
		if(manageTxn){
			return new TxnManagedMultiIndexNode<>(backingNode, params, indexName);
		}
		return new NoTxnManagedMultiIndexNode<>(backingNode, params, indexName);
	}

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IK extends PrimaryKey<IK>,
					IE extends MultiIndexEntry<IK,IE,PK,D>,
					IF extends DatabeanFielder<IK,IE>>
	ManagedMultiIndexNode<PK, D, IK, IE, IF> newManagedMulti(Router router,
			IndexedMapStorage<PK, D> backingNode, Class<IF> indexFielder, Class<IE> indexEntryClass,
			boolean manageTxn){
		return newManagedMulti(router, backingNode, indexFielder, indexEntryClass, manageTxn,
			indexEntryClass.getSimpleName());
	}
}
