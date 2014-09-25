package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedUniqueIndexNode;
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
public class HibernateClientType
implements ClientType{
	private static final Logger logger = LoggerFactory.getLogger(HibernateClientType.class);
	
	public static final String NAME = "hibernate";
	
	public static final HibernateClientType INSTANCE = new HibernateClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new HibernateSimpleClientFactory(drContext, clientName); 
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		if(nodeParams.getFielderClass() == null){
			Node<?,?> node = new HibernateNode(nodeParams);
			logger.warn("creating HibernateNode "+node);
			return node;
		}
		return new JdbcNode(nodeParams);
	}
	
	//ignore the entityNodeParams
	@Override
	public Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams){
		return createNode(nodeParams);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends UniqueIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder){
		return new JdbcManagedUniqueIndexNode<PK, D, IK, IE, IF>(backingMapNode, indexEntryClass, indexFielder);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>, 
			IK extends PrimaryKey<IK>, 
			IE extends MultiIndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> ManagedMultiIndexNode<PK, D, IK, IE> createManagedMultiIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder){
		return new JdbcManagedMultiIndexNode<PK,D,IK,IE,IF>(backingMapNode, indexEntryClass, indexFielder);
	}
	
}
