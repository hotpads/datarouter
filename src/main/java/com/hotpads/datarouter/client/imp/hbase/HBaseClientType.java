package com.hotpads.datarouter.client.imp.hbase;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

@Singleton
public class HBaseClientType
implements ClientType{
	
	public static final String NAME = "hbase";
	
	public static final HBaseClientType INSTANCE = new HBaseClientType();
	
	@Override
	public String getName(){
		return NAME;
	}
	
	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		// if(USE_RECONNECTING_HBASE_CLIENT){
		// return new HBaseDynamicClientFactory(router, clientName,
		// configFileLocation, executorService);
		// }else{
		return new HBaseSimpleClientFactory(drContext, clientName);
		// }
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		return new HBaseNode(nodeParams);
	}
	
	@Override
	public Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams){
		return new HBaseSubEntityNode(entityNodeParams, nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends UniqueIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder){
		throw new UnsupportedOperationException(getName() + " does not support managed indexes");
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends MultiIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedMultiIndexNode<PK, D, IK, IE> createManagedMultiIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder){
		throw new UnsupportedOperationException(getName() + " does not support managed indexes");
	}

}
