package com.hotpads.datarouter.client.imp;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

public abstract class BaseClientType implements ClientType{

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends UniqueIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE, IF> createManagedUniqueIndexNode(PhysicalMapStorageNode<PK, D> backingMapNode,
			NodeParams<IK, IE, IF> params, String indexName, boolean manageTxn){
		throw new UnsupportedOperationException(getClass() + " does not support managed indexes");
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends MultiIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedMultiIndexNode<PK, D, IK, IE, IF> createManagedMultiIndexNode(PhysicalMapStorageNode<PK, D> backingMapNode, 
			NodeParams<IK, IE, IF> params, String indexName, boolean manageTxn){
		throw new UnsupportedOperationException(getClass() + " does not support managed indexes");
	}

}
