package com.hotpads.datarouter.node.factory;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.adapter.IndexedSortedMapStorageAdapterNode;
import com.hotpads.datarouter.node.adapter.MapStorageAdapterNode;
import com.hotpads.datarouter.node.adapter.SortedMapStorageAdapterNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.StorageType;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

@Singleton
public class MasterSlaveNodeFactory{

	private final NodeFactory nodeFactory;
	
	@Inject
	public MasterSlaveNodeFactory(NodeFactory nodeFactory){
		this.nodeFactory = nodeFactory;
	}


	//no Fielder
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	BaseNode<PK,D,?> 
	create(
			DataRouter router, StorageType storageType, Class<D> databeanClass,
			String masterClientName, Collection<String> slaveClientNames){
		return createInternal(router, storageType, databeanClass, null, masterClientName, slaveClientNames);
	}
	
	
	/************** convenience methods that (try to) cast to the desired type *****************/
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	MapStorageNode<PK,D> createMap(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (MapStorageNode<PK,D>)createInternal(router, StorageType.map, databeanClass,
				fielderClass, masterClientName, slaveClientNames);
	}
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	SortedMapStorageNode<PK,D> createSorted(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (SortedMapStorageNode<PK,D>)createInternal(router, StorageType.sortedMap, databeanClass,
				fielderClass, masterClientName, slaveClientNames);
	}
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	IndexedSortedMapStorageNode<PK,D> createIndexed(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (IndexedSortedMapStorageNode<PK,D>)createInternal(router, StorageType.indexed,
				databeanClass, fielderClass, masterClientName, slaveClientNames);
	}

	
	/************** private ******************/
	
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	BaseNode<PK,D,F> createInternal(DataRouter router, StorageType storageType, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		
		//create the backing nodes
		N master = null;
		if(masterClientName != null){
			master = nodeFactory.create(masterClientName, databeanClass, fielderClass, router, false);
		}
		
		List<N> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			N slaveNode = nodeFactory.create(slaveClientName, databeanClass, fielderClass, router, false);
			slaves.add(slaveNode);
		}
		
		//create the parent node.  cast the master/slave nodes so we throw an error here if they aren't the right type
		if(StorageType.map == storageType){
			MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>> backingNode 
					= new MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>>(
					databeanClass, router, 
					(MapStorageNode<PK,D>)master, (List<MapStorageNode<PK,D>>)slaves);
			return new MapStorageAdapterNode(databeanClass, router, backingNode);
		}else if(StorageType.sortedMap == storageType){
			MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>> backingNode 
					= new MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>>(
					databeanClass, router, 
					(SortedMapStorageNode<PK,D>)master, (List<SortedMapStorageNode<PK,D>>)slaves);
			return new SortedMapStorageAdapterNode(databeanClass, router, backingNode);
		}else if(StorageType.indexed == storageType){
			MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>> backingNode 
					= new MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>>(
					databeanClass, router, 
					(IndexedSortedMapStorageNode<PK,D>)master, (List<IndexedSortedMapStorageNode<PK,D>>)slaves);
			return new IndexedSortedMapStorageAdapterNode(databeanClass, router, backingNode);
		}else{
			throw new IllegalArgumentException("StorageType "+storageType+" not supported");
		}
		
	}
	

}
