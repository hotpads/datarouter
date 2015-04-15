package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.client.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcManagedUniqueIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedMultiIndexNode;
import com.hotpads.datarouter.client.imp.jdbc.node.index.JdbcTxnManagedUniqueIndexNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
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
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		PhysicalIndexedSortedMapStorageNode<PK,D> node;
		if(nodeParams.getFielderClass() == null){
			node = new PhysicalIndexedSortedMapStorageCounterAdapter<PK,D,F,HibernateNode<PK,D,F>>(
					new HibernateNode<PK,D,F>(nodeParams));
//			logger.warn("creating HibernateNode "+node);
		}else{
			node = new PhysicalIndexedSortedMapStorageCounterAdapter<PK,D,F,JdbcNode<PK,D,F>>(
					new JdbcNode<PK,D,F>(nodeParams));
		}
		return node;
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
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<PK,D,F,PhysicalIndexedSortedMapStorageNode<PK,D>>(
				nodeParams, (PhysicalIndexedSortedMapStorageNode<PK,D>)backingNode);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>, 
			IK extends PrimaryKey<IK>, 
			IE extends UniqueIndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> ManagedUniqueIndexNode<PK, D, IK, IE, IF> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, NodeParams<IK, IE, IF> params, String indexName, 
			boolean manageTxn){
		if(!(backingMapNode.getPhysicalNodeIfApplicable() instanceof JdbcNode)){
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
		if(!(backingMapNode.getPhysicalNodeIfApplicable() instanceof JdbcNode)){
			super.createManagedMultiIndexNode(backingMapNode, params, indexName, manageTxn);
		}
		if(manageTxn){
			return new JdbcTxnManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
		}
		return new JdbcManagedMultiIndexNode<PK, D, IK, IE, IF>(backingMapNode, params, indexName);
	}
	
}
