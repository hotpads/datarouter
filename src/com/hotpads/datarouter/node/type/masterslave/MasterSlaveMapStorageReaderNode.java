package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.masterslave.base.BaseMasterSlaveNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class MasterSlaveMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseMasterSlaveNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{
	
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
	public boolean exists(PK key, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.exists(key, config);
	}

	@Override
	public D get(PK key, Config config) {
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
	public List<D> getMulti(Collection<PK> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? this.chooseSlave(config) : this.master;
		return node.getKeys(keys, config);
	}

	
}
