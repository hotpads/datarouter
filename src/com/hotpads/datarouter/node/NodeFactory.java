package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class NodeFactory {

	public static <D extends Databean> 
	MasterSlaveSortedStorageNode<D,SortedStorageNode<D>> 
	newMasterSlaveSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		SortedStorageNode<D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D>(databeanClass, router, masterClientName);
		}
		
		List<SortedStorageNode<D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveSortedStorageNode<D,SortedStorageNode<D>>(
				databeanClass, router, master, slaves);
		
	}

	public static <D extends Databean> 
	MasterSlaveIndexedSortedStorageNode<D,IndexedSortedStorageNode<D>> 
	newMasterSlaveIndexedSorted(
			DataRouter router, Class<D> databeanClass, 
			String masterClientName, Collection<String> slaveClientNames){
		
		IndexedSortedStorageNode<D> master = null;
		if(masterClientName != null){
			master = new HibernateNode<D>(databeanClass, router, masterClientName);
		}
		
		List<IndexedSortedStorageNode<D>> slaves = ListTool.createLinkedList();
		for(String slaveClientName : CollectionTool.nullSafe(slaveClientNames)){
			slaves.add(new HibernateNode<D>(databeanClass, router, slaveClientName));
		}
		
		return new MasterSlaveIndexedSortedStorageNode<D,IndexedSortedStorageNode<D>>(
				databeanClass, router, master, slaves);
		
	}
}
