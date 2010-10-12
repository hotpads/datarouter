package com.hotpads.datarouter.node.base.masterslave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseMasterSlaveNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D>{
	
	protected N master;
	protected List<N> slaves = new ArrayList<N>();
	
	protected List<N> masterAndSlaves = new ArrayList<N>();
	
	protected AtomicInteger slaveRequestCounter = new AtomicInteger(0);
	
	public BaseMasterSlaveNode(Class<D> databeanClass, DataRouter router){
		super(databeanClass);
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(this.name);
		names.addAll(this.master.getAllNames());
		for(N slave : IterableTool.nullSafe(this.slaves)){
			names.addAll(slave.getAllNames());
		}
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		all.addAll(this.master.getPhysicalNodes());
		for(N slave : CollectionTool.nullSafe(this.slaves)){
			all.addAll(ListTool.nullSafe(slave.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
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
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return this.master.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
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
		this.masterAndSlaves.add(master);
		return master;
	}
	
	public N registerSlave(N slave){
		this.slaves.add(slave);
		this.masterAndSlaves.add(slave);
		return slave;
	}
	
	
	
	@Override
	public N getMaster() {
		return this.master;
	}
	
	@Override
	public List<N> getChildNodes(){
		return masterAndSlaves;
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
