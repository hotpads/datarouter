package com.hotpads.datarouter.client.imp.jdbc;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedUniqueIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedUniqueIndexNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.IndexedSortedMapStorageAdapterNode;
import com.hotpads.datarouter.node.adapter.counter.IndexedSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

@Singleton
public class JdbcClientType extends BaseClientType{
	
	public static final JdbcClientType INSTANCE = new JdbcClientType();
	
	public static final String NAME = "jdbc";
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new JdbcSimpleClientFactory(drContext, clientName);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	Node<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new IndexedSortedMapStorageCounterAdapter(new JdbcNode<PK,D,F>(nodeParams));
	}
	
	//ignore the entityNodeParams
	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK,D> createSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> nodeParams){
		return createNode(nodeParams);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>> 
	IndexedSortedMapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new IndexedSortedMapStorageAdapterNode<PK, D, F, IndexedSortedMapStorageNode<PK, D>>(nodeParams,
				(IndexedSortedMapStorageNode<PK, D>) backingNode);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>, 
			IK extends PrimaryKey<IK>, 
			IE extends UniqueIndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> ManagedUniqueIndexNode<PK, D, IK, IE, IF> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, NodeParams<IK, IE, IF> params, String indexName, 
			boolean manageTxn){
		if(manageTxn){
			return new JdbcTxnManagedUniqueIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
		}
		return new JdbcManagedUniqueIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>, 
			IK extends PrimaryKey<IK>, 
			IE extends MultiIndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> ManagedMultiIndexNode<PK, D, IK, IE, IF> createManagedMultiIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, NodeParams<IK, IE, IF> params, String indexName, 
			boolean manageTxn){
		if(manageTxn){
			return new JdbcTxnManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
		}
		return new JdbcManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
	}
	
}
