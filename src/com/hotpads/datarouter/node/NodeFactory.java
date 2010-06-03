package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class NodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		
		return new HibernateNode<PK,D>(databeanClass, router, clientName);
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
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
	MasterSlaveSortedStorageNode<PK,D,SortedStorageNode<PK,D>> 
	newMasterSlaveSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		SortedStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<SortedStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveSortedStorageNode<PK,D,SortedStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	MasterSlaveIndexedStorageNode<PK,D,IndexedStorageNode<PK,D>> 
	newMasterSlaveIndexed(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<IndexedStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedStorageNode<PK,D,IndexedStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	MasterSlaveIndexedSortedStorageNode<PK,D,IndexedSortedStorageNode<PK,D>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedSortedStorageNode<PK,D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<PK,D>(databeanClass, router, masterClientName);
		}
		
		List<IndexedSortedStorageNode<PK,D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<PK,D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedSortedStorageNode<PK,D,IndexedSortedStorageNode<PK,D>>(
				databeanClass, router, master, slaves);
		
	}
}









