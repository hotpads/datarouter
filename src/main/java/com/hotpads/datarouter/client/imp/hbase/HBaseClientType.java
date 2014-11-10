package com.hotpads.datarouter.client.imp.hbase;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.SortedMapStorageAdapterNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class HBaseClientType extends BaseClientType{
	
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
	public Node<?,?> createAdapter(NodeParams<?,?,?> nodeParams, Node<?,?> backingNode){
		return new SortedMapStorageAdapterNode(nodeParams.getDatabeanClass(), nodeParams.getRouter(), 
				(SortedMapStorageNode<?,?>) backingNode);
	}
}
