package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedUniqueIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedUniqueIndexNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.IndexedSortedMapStorageAdapterNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

@Singleton
public class HibernateClientType extends BaseClientType{
	private static final Logger logger = LoggerFactory.getLogger(HibernateClientType.class);
	
	public static final String NAME = "hibernate";
	
	public static final HibernateClientType INSTANCE = new HibernateClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new HibernateSimpleClientFactory(drContext, clientName); 
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		IndexedSortedMapStorageNode<?,?> node;
		if(nodeParams.getFielderClass() == null){
			node = new HibernateNode(nodeParams);
			logger.warn("creating HibernateNode "+node);
		}else{
			node = new JdbcNode(nodeParams);
		}
		return node;
	}
	
	//ignore the entityNodeParams
	@Override
	public Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams){
		return createNode(nodeParams);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	IndexedSortedMapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new IndexedSortedMapStorageAdapterNode(nodeParams, (IndexedSortedMapStorageNode<PK,D>)backingNode);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>, 
			IK extends PrimaryKey<IK>, 
			IE extends UniqueIndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> ManagedUniqueIndexNode<PK, D, IK, IE, IF> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, NodeParams<IK, IE, IF> params, String indexName, 
			boolean manageTxn){
		if(!(backingMapNode instanceof JdbcNode)){
			super.createManagedUniqueIndexNode(backingMapNode, params, indexName, manageTxn);
		}
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
		if(!(backingMapNode instanceof JdbcNode)){
			super.createManagedMultiIndexNode(backingMapNode, params, indexName, manageTxn);
		}
		if(manageTxn){
			return new JdbcTxnManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
		}
		return new JdbcManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
	}
	
}
