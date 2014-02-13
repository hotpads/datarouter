package com.hotpads.datarouter.client.imp.http;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.http.node.HttpReaderNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class HttpClientType<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements ClientType<PK,D,F>{
	
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
	public Node<PK,D> createNode(NodeParams<PK,D,F> nodeParams){
		return new HttpReaderNode<PK,D,F>(nodeParams);//TODO change to HttpNode when it's available
	}
	
}
