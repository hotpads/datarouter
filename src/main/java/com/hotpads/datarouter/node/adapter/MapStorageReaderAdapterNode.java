package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.profile.callsite.LineOfCode;

public class MapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseAdapterNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{

	private N backingNode;

	public MapStorageReaderAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withFielder((Class<F>)backingNode.getFieldInfo().getFielderClass())
				.build());
		this.backingNode = backingNode;
	}

	
	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		return backingNode.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig) {
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		return backingNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig) {
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite());
		return backingNode.getKeys(keys, config);
	}
	
	
	/********************* helper ***************************************/
	
	protected LineOfCode getCallsite(){
		return new LineOfCode(2);//adjust for this method and adapter method
	}
	
}
