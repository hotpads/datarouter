package com.hotpads.datarouter.client.imp.http;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.http.node.HttpReaderNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.MapStorageReaderAdapterNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class HttpClientType extends BaseClientType{
	
	public static final String NAME = "http";
	
	public static final HttpClientType INSTANCE = new HttpClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new DataRouterHttpClientFactory(drContext, clientName);
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		return new HttpReaderNode(nodeParams);//TODO change to HttpNode when it's available
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
	MapStorageReaderNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new MapStorageReaderAdapterNode(nodeParams, (MapStorageReaderNode<PK,D>)backingNode);
	}
}
