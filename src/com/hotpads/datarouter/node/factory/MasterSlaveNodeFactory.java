package com.hotpads.datarouter.node.factory;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class MasterSlaveNodeFactory{
	
	//Map
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	MasterSlaveMapStorageNode<PK,D,?,MapStorageNode<PK,D>> 
	newMasterSlaveMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		return newMasterSlaveMap(router, databeanClass, null, masterClientName, slaveClientNames);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>> 
	newMasterSlaveMap(
			DataRouter router, Class<D> databeanClass, Class<F> fielderClass,
			String masterClientName, Collection<String> slaveClientNames){
		
		MapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, masterClientName);
		}
		
		List<MapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, slaveClientName));
		}
		
		return new MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}
	
	
	//SortedMap
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	MasterSlaveSortedMapStorageNode<PK,D,?,SortedMapStorageNode<PK,D>> 
	newMasterSlaveSortedMap(
			DataRouter router, Class<D> databeanClass,
			String masterClientName, Collection<String> slaveClientNames){
		return newMasterSlaveSortedMap(router, databeanClass, null, masterClientName, slaveClientNames);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>> 
	newMasterSlaveSortedMap(
			DataRouter router, Class<D> databeanClass, Class<F> fielderClass,
			String masterClientName, Collection<String> slaveClientNames){
		
		SortedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, masterClientName);
		}
		
		List<SortedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, slaveClientName));
		}
		
		return new MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}
	
	
	//IndexedMap
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	MasterSlaveIndexedMapStorageNode<PK,D,?,IndexedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		return newMasterSlaveIndexedMap(router, databeanClass, null, masterClientName, slaveClientNames);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	MasterSlaveIndexedMapStorageNode<PK,D,F,IndexedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedMap(
			DataRouter router, Class<D> databeanClass, Class<F> fielderClass,
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, masterClientName);
		}
		
		List<IndexedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedMapStorageNode<PK,D,F,IndexedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	
	//IndexedSorted
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	MasterSlaveIndexedSortedMapStorageNode<PK,D,?,IndexedSortedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		return newMasterSlaveIndexedSorted(router, databeanClass, null, masterClientName, slaveClientNames);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, Class<F> fielderClass,
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedSortedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, masterClientName);
		}
		
		List<IndexedSortedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

}
