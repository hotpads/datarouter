package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.masterslave.BaseMasterSlaveNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;

public class MasterSlaveMapStorageReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>,
N extends MapStorageReaderNode<D,PK>>
extends BaseMasterSlaveNode<D,PK,N>
implements MapStorageReaderNode<D,PK>{
	
	public MasterSlaveMapStorageReaderNode(Class<D> databeanClass, DataRouter router,
			N master, Collection<N> slaves) {
		super(databeanClass, router);
		
		if(master!=null){ this.registerMaster(master); 
		}
		for(N slave : CollectionTool.nullSafe(slaves)){
			this.registerSlave(slave);
		}
	}
	
	public MasterSlaveMapStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(UniqueKey<PK> key, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.exists(key, config);
	}

	@Override
	public D get(UniqueKey<PK> key, Config config) {
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
	public List<D> getMulti(Collection<? extends UniqueKey<PK>> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getMulti(keys, config);
	}

	
}
