package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class MapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageReaderAdapterNode<PK,D,F,N>
implements MapStorageNode<PK,D>{

	public MapStorageAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
	}
	
	
	/**************************** MapStorage ***********************************/

	@Override
	public void put(D databean, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_put, 1));
		backingNode.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		int numItems = CollectionTool.size(databeans);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_putMulti, numItems));
		backingNode.putMulti(databeans, config);
	}

	@Override
	public void delete(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_delete, 1));
		backingNode.delete(key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		int numItems = CollectionTool.size(keys);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteMulti, numItems));
		backingNode.deleteMulti(keys, config);
	}

	@Override
	public void deleteAll(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteAll, 0));
		backingNode.deleteAll(config);
	}

	
}
