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
	public Node<?,?> createAdapter(NodeParams<?,?,?> nodeParams, Node<?,?> backingNode){
		return new MapStorageReaderAdapterNode(nodeParams.getDatabeanClass(), nodeParams.getRouter(), 
				(MapStorageReaderNode<?,?>)backingNode);
	}
}
