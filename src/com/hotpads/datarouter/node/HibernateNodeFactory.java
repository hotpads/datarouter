package com.hotpads.datarouter.node;

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
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HibernateNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		
		return new HibernateNode<PK,D>(databeanClass, router, clientName);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			DataRouter router){
		
		HibernateNode<PK,D> node = new HibernateNode<PK,D>(databeanClass, router, clientName,
				physicalName, qualifiedPhysicalName);
		return node;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		
		HibernateNode<PK,D> node = new HibernateNode<PK,D>(
				baseDatabeanClass, databeanClass, router, clientName);
		return node;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>>
	MasterSlaveMapStorageNode<PK,D,MapStorageNode<PK,D>> 
	newMasterSlaveMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		MapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<MapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveMapStorageNode<PK,D,MapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>>
	MasterSlaveSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>> 
	newMasterSlaveSortedMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		SortedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<SortedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveSortedMapStorageNode<PK,D,SortedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>>
	MasterSlaveIndexedMapStorageNode<PK,D,IndexedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<IndexedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedMapStorageNode<PK,D,IndexedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>>
	MasterSlaveIndexedSortedMapStorageNode<PK,D,IndexedSortedMapStorageNode<PK,D>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedSortedMapStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<IndexedSortedMapStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedSortedMapStorageNode<PK,D,IndexedSortedMapStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}
}









