package com.hotpads.datarouter.node.type.masterslave.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;

public abstract class BaseMasterSlaveNode<
				PK extends PrimaryKey<PK>,
				D extends Databean<PK,D>,
				F extends DatabeanFielder<PK,D>,
				N extends Node<PK,D>>
extends BaseNode<PK,D,F> implements MasterSlaveNode<PK,D,N>{

	protected N master;
	protected List<N> slaves = new ArrayList<>();

	protected List<N> masterAndSlaves = new ArrayList<>();

	protected AtomicInteger slaveRequestCounter = new AtomicInteger(0);

	public BaseMasterSlaveNode(Supplier<D> databeanSupplier, Router router, N master, Collection<N> slaves){
		super(new NodeParamsBuilder<>(router, databeanSupplier,
				(Supplier<F>)master.getFieldInfo().getFielderSupplier())
				.build());

		this.registerMaster(master);
		for(N slave : DrCollectionTool.nullSafe(slaves)){
			this.registerSlave(slave);
		}
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = DrSetTool.wrap(getName());
		names.addAll(this.master.getAllNames());
		for(N slave : DrIterableTool.nullSafe(this.slaves)){
			names.addAll(slave.getAllNames());
		}
		return names;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
		all.addAll(this.master.getPhysicalNodes());
		for(N slave : DrCollectionTool.nullSafe(this.slaves)){
			all.addAll(DrListTool.nullSafe(slave.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
		all.addAll(this.master.getPhysicalNodesForClient(clientName));
		for(N slave : DrCollectionTool.nullSafe(this.slaves)){
			all.addAll(DrListTool.nullSafe(slave.getPhysicalNodesForClient(clientName)));
		}
		return all;
	}


	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = new TreeSet<>();
		DrSetTool.nullSafeSortedAddAll(clientNames, this.master.getClientNames());
		for(N slave : this.slaves){
			DrSetTool.nullSafeSortedAddAll(clientNames, slave.getClientNames());
		}
		return new ArrayList<>(clientNames);
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>(master.getClientIds());
		for(N slave : this.slaves){
			clientIds.addAll(slave.getClientIds());
		}
		return new ArrayList<>(clientIds);
	}

	@Override
	public boolean usesClient(String clientName){
		if(this.master.usesClient(clientName)){
			return true;
		}
		for(N slave : DrCollectionTool.nullSafe(this.slaves)){
			if(slave.usesClient(clientName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return this.master.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
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

	@Override
	public N chooseSlave(Config config){
//		Config c = Config.nullSafe(config);
		//may want to use the config to get the same slave as was used previously
		int numSlaves = DrCollectionTool.sizeNullSafe(this.slaves);
		if(numSlaves==0){
			return master;
		}
		int slaveNum = this.slaveRequestCounter.incrementAndGet() % numSlaves;
		return this.slaves.get(slaveNum);
	}

}
