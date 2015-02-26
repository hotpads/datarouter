package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public abstract class PartitionedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageReaderNode<PK,D>>
extends BasePartitionedNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{
	protected static Logger logger = LoggerFactory.getLogger(PartitionedMapStorageReaderNode.class);
	
	public PartitionedMapStorageReaderNode(Class<D> databeanClass, Class<F> fielderClass, Datarouter router) {
		super(databeanClass, fielderClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		N node = getPhysicalNode(key);
		if(node==null){ return false; }
		if(node.exists(key, config)){
			return true;
		}
		return false;
	}

	@Override
	public D get(PK key, Config config) {
		N node = getPhysicalNode(key);
		if(node==null){ return null; }
		D databean = node.get(key, config);
		if(databean != null){
			return databean;
		}
		return null;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		Multimap<N,PK> keysByNode = getPrimaryKeysByPhysicalNode(keys);
//		logger.warn(keysByNode);
		List<D> all = DrListTool.createArrayList();
		if(keysByNode==null){ return all; }
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(DrCollectionTool.isEmpty(keysForNode)){ continue; }//should not be empty, but being safer
			List<D> databeansFromNode = node.getMulti(keysForNode, config);
			all.addAll(DrCollectionTool.nullSafe(databeansFromNode));
		}
		return all;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		Multimap<N,PK> keysByNode = getPrimaryKeysByPhysicalNode(keys);
		List<PK> all = DrListTool.createArrayList();
		if(keysByNode==null){ return all; }
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(DrCollectionTool.isEmpty(keysForNode)){ continue; }//should not be empty, but being safer
			List<PK> pksFromNode = node.getKeys(keysForNode, config);
			all.addAll(DrCollectionTool.nullSafe(pksFromNode));
		}
		return all;
	}

}
