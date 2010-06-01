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

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	HibernateNode<D,PK> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		
		return new HibernateNode<D,PK>(databeanClass, router, clientName);
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	HibernateNode<D,PK> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			DataRouter router){
		
		HibernateNode<D,PK> node = new HibernateNode<D,PK>(databeanClass, router, clientName,
				physicalName, qualifiedPhysicalName);
		return node;
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	MasterSlaveMapStorageNode<D,PK,MapStorageNode<D,PK>> 
	newMasterSlaveMap(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		MapStorageNode<D,PK> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D,PK>(databeanClass, router, masterClientName);
		}
		
		List<MapStorageNode<D,PK>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D,PK>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveMapStorageNode<D,PK,MapStorageNode<D,PK>>(
				databeanClass, router, master, slaves);
		
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	MasterSlaveSortedStorageNode<D,PK,SortedStorageNode<D,PK>> 
	newMasterSlaveSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		SortedStorageNode<D,PK> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D,PK>(databeanClass, router, masterClientName);
		}
		
		List<SortedStorageNode<D,PK>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D,PK>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveSortedStorageNode<D,PK,SortedStorageNode<D,PK>>(
				databeanClass, router, master, slaves);
		
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	MasterSlaveIndexedStorageNode<D,PK,IndexedStorageNode<D,PK>> 
	newMasterSlaveIndexed(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedStorageNode<D,PK> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D,PK>(databeanClass, router, masterClientName);
		}
		
		List<IndexedStorageNode<D,PK>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D,PK>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedStorageNode<D,PK,IndexedStorageNode<D,PK>>(
				databeanClass, router, master, slaves);
		
	}

	public static <D extends Databean<PK>,PK extends PrimaryKey<PK>> 
	MasterSlaveIndexedSortedStorageNode<D,PK,IndexedSortedStorageNode<D,PK>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedSortedStorageNode<D,PK> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D,PK>(databeanClass, router, masterClientName);
		}
		
		List<IndexedSortedStorageNode<D,PK>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D,PK>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedSortedStorageNode<D,PK,IndexedSortedStorageNode<D,PK>>(
				databeanClass, router, master, slaves);
		
	}
}









