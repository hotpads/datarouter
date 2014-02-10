package com.hotpads.datarouter.node.factory;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.StorageType;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class MasterSlaveNodeFactory{
	
	//no Fielder
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	BaseMasterSlaveNode<PK,D,?,?> 
	create(
			DataRouter router, StorageType storageType, Class<D> databeanClass,
			String masterClientName, Collection<String> slaveClientNames){
		return createInternal(router, storageType, databeanClass, null, masterClientName, slaveClientNames);
	}
	
	
	/************** convenience methods that (try to) cast to the desired type *****************/
	
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	MasterSlaveMapStorageNode<PK,D,F,?> createMap(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (MasterSlaveMapStorageNode<PK,D,F,?>)createInternal(router, StorageType.map, databeanClass,
				fielderClass, masterClientName, slaveClientNames);
	}
	
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	MasterSlaveSortedMapStorageNode<PK,D,F,?> createSorted(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (MasterSlaveSortedMapStorageNode<PK,D,F,?>)createInternal(router, StorageType.sortedMap, databeanClass,
				fielderClass, masterClientName, slaveClientNames);
	}
	
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	MasterSlaveIndexedSortedMapStorageNode<PK,D,F,?> createIndexed(DataRouter router, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		return (MasterSlaveIndexedSortedMapStorageNode<PK,D,F,?>)createInternal(router, StorageType.indexed,
				databeanClass, fielderClass, masterClientName, slaveClientNames);
	}

	
	/************** private ******************/
	
	private static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	BaseMasterSlaveNode<PK,D,F,?> createInternal(DataRouter router, StorageType storageType, Class<D> databeanClass,
			Class<F> fielderClass, String masterClientName, Collection<String> slaveClientNames){
		
		//create the backing nodes
		N master = null;
		if(masterClientName != null){
			master = NodeFactory.create(masterClientName, databeanClass, fielderClass, router);
		}
		
		List<N> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			N slaveNode = NodeFactory.create(slaveClientName, databeanClass, 
					fielderClass, router);
			slaves.add(slaveNode);
		}
		
		//create the parent node.  cast the master/slave nodes so we throw an error here if they aren't the right type
		if(StorageType.map == storageType){
			return new MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>>(
					databeanClass, router, 
					(MapStorageNode<PK,D>)master, (List<MapStorageNode<PK,D>>)slaves);
		}else if(StorageType.sortedMap == storageType){
			return new MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>>(
					databeanClass, router, 
					(SortedMapStorageNode<PK,D>)master, (List<SortedMapStorageNode<PK,D>>)slaves);
		}else if(StorageType.indexed == storageType){
			return new MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>>(
					databeanClass, router, 
					(IndexedSortedMapStorageNode<PK,D>)master, (List<IndexedSortedMapStorageNode<PK,D>>)slaves);
		}else{
			throw new IllegalArgumentException("StorageType "+storageType+" not supported");
		}
		
	}
	

}
