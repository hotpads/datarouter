package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;

public class JdbcTxnManagedMultiIndexNode<
		PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>, 
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK, IE, PK, D>, 
		IF extends DatabeanFielder<IK, IE>>
extends BaseJdbcManagedIndexNode<PK, D, IK, IE, IF>
implements ManagedMultiIndexNode<PK, D, IK, IE, IF>{
	
	public JdbcTxnManagedMultiIndexNode(IndexedMapStorageNode<PK,D> node, NodeParams<IK,IE,IF> params,
			String name){
		super(node, params, name);
	}

	@Override
	public List<D> lookupMulti(IK indexKey, Config config){
		return lookupMultiMulti(Collections.singleton(indexKey), config);
	}

	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config){
		return node.getMultiByIndex(indexKeys, config);
	}

}
