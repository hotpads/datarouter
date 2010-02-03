package com.hotpads.datarouter.node.base.masterslave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseMasterSlaveNode<D extends Databean,N extends Node<D>> 
implements Node<D>, MapStorageReadOps<D> {


	protected Class<D> databeanClass;
	protected N master;
	protected List<N> slaves = new ArrayList<N>();
	protected String name;
	
	protected DataRouter router;
	
	protected AtomicInteger slaveRequestCounter = new AtomicInteger(0);
	
	public BaseMasterSlaveNode(Class<D> databeanClass, DataRouter router){
		this.databeanClass = databeanClass;
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();
		this.router = router;
	}

	/*************************** node methods *************************/
	
	@Override
	public Class<D> getDatabeanType() {
		return this.databeanClass;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<PhysicalNode<D>> getPhysicalNodes() {
		List<PhysicalNode<D>> all = ListTool.createLinkedList();
		all.addAll(this.master.getPhysicalNodes());
		for(N slave : CollectionTool.nullSafe(this.slaves)){
			all.addAll(ListTool.nullSafe(slave.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<D>> all = ListTool.createLinkedList();
		all.addAll(this.master.getPhysicalNodesForClient(clientName));
		for(N slave : CollectionTool.nullSafe(this.slaves)){
			all.addAll(ListTool.nullSafe(slave.getPhysicalNodesForClient(clientName)));
		}
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		SetTool.nullSafeSortedAddAll(clientNames, this.master.getClientNames());
		for(N slave : this.slaves){
			SetTool.nullSafeSortedAddAll(clientNames, slave.getClientNames());
		}
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		if(this.master.usesClient(clientName)){ return true; }
		for(N slave : CollectionTool.nullSafe(this.slaves)){
			if(slave.usesClient(clientName)){ return true; }
		}
		return false;
	}

	@Override
	public <K extends Key<D>> List<String> getClientNamesForKeys(Collection<K> keys) {
		return this.master.getClientNamesForKeys(keys);
	}
	
	@Override
	public void clearThreadSpecificState(){
		if(this.master!=null){ this.master.clearThreadSpecificState(); }
		for(N slave : CollectionTool.nullSafe(this.slaves)){
			slave.clearThreadSpecificState();
		}
	}
	
	/************************ masterslave node methods ***************************/
	
	public N registerMaster(N master){
		this.master = master;
		return master;
	}
	
	public N registerSlave(N slave){
		this.slaves.add(slave);
		return slave;
	}
	
	
	
	@Override
	public N getMaster() {
		return this.master;
	}

	public N chooseSlave(Config config){
//		Config c = Config.nullSafe(config);
		//may want to use the config to get the same slave as was used previously
		int numSlaves = CollectionTool.sizeNullSafe(this.slaves);
		if(numSlaves==0){ return master; }
		int slaveNum = this.slaveRequestCounter.incrementAndGet() % numSlaves;
		return this.slaves.get(slaveNum);
	}
	
}
