package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class JdbcTxnManagedUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>, 
		IE extends UniqueIndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseJdbcManagedIndexNode<PK,D,IK,IE,IF>
implements ManagedUniqueIndexNode<PK, D, IK, IE, IF>{

	public JdbcTxnManagedUniqueIndexNode(IndexedMapStorageNode<PK, D> node, NodeParams<IK, IE, IF> params, String name){
		super(node, params, name);
	}

	@Override
	public D lookupUnique(IK indexKey, Config config){
		return DrCollectionTool.getFirst(lookupMultiUnique(Collections.singleton(indexKey), config));
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		return node.getMultiByIndex(uniqueKeys, config);
	}
	
	@Override
	public IE get(IK uniqueKey, Config config){
		return DrCollectionTool.getFirst(getMulti(Collections.singleton(uniqueKey), config));
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, Config config){
		return node.getMultiFromIndex(uniqueKeys, config, fieldInfo);
	}

	@Override
	public void deleteUnique(IK indexKey, Config config){
		deleteMultiUnique(Collections.singleton(indexKey), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
		node.deleteByIndex(uniqueKeys, config);
	}

}
