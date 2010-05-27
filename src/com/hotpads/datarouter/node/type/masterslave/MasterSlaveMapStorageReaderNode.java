package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.masterslave.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class MasterSlaveMapStorageReaderNode<D extends Databean,
PK extends PrimaryKey<D>,N extends MapStorageReaderNode<D,PK>>
extends BaseMasterSlaveNode<D,PK,N>
implements MapStorageReaderNode<D,PK>{
	
	public MasterSlaveMapStorageReaderNode(Class<PK> primaryKeyClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(primaryKeyClass, router);
		
		if(master!=null){ this.registerMaster(master); 
		}
		for(N slave : CollectionTool.nullSafe(slaves)){
			this.registerSlave(slave);
		}
	}
	
	public MasterSlaveMapStorageReaderNode(Class<PK> primaryKeyClass, DataRouter router) {
		super(primaryKeyClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(UniqueKey<D> key, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.exists(key, config);
	}

	@Override
	public D get(UniqueKey<D> key, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.get(key, config);
	}

	@Override
	public List<D> getAll(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getAll(config);
	}

	@Override
	public List<D> getMulti(Collection<? extends UniqueKey<D>> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getMulti(keys, config);
	}

	
}
