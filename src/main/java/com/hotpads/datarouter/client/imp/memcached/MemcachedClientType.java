package com.hotpads.datarouter.client.imp.memcached;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.MapStorageAdapterNode;
import com.hotpads.datarouter.node.adapter.counter.MapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class MemcachedClientType extends BaseClientType{
	
	public static final String NAME = "memcached";
	
	public static final MemcachedClientType INSTANCE = new MemcachedClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new MemcachedSimpleClientFactory(drContext, clientName);
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		return new MapStorageCounterAdapter(new MemcachedNode(nodeParams));
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
	MapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new MapStorageAdapterNode(nodeParams, (MapStorageNode<PK,D>)backingNode);
	}
	
}
