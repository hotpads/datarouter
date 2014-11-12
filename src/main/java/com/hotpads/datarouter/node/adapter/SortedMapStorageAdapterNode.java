package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends SortedMapStorageReaderAdapterNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>{
	
	public SortedMapStorageAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
	}

	
	/**************************** MapStorage ***********************************/
	//copy pasted from MapStorageAdapterNode.  use a mixin?
	
	@Override
	public void put(D databean, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_put));
		backingNode.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_putMulti));
		backingNode.putMulti(databeans, config);
	}

	@Override
	public void delete(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_delete));
		backingNode.delete(key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteMulti));
		backingNode.deleteMulti(keys, config);
	}

	@Override
	public void deleteAll(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(MapStorageWriter.OP_deleteAll));
		backingNode.deleteAll(config);
	}
	

	/***************** SortedStorageWriter ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageWriter.OP_deleteRangeWithPrefix));
		backingNode.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
}
