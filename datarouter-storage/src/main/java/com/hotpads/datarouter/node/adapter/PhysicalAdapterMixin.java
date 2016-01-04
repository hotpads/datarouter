package com.hotpads.datarouter.node.adapter;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalAdapterMixin<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends PhysicalNode<PK,D>>
extends PhysicalNode<PK,D>{

	N getBackingNode();

	@Override
	public default ClientId getClientId(){
		return getBackingNode().getClientId();
	}


	@Override
	public default Client getClient(){
		return getBackingNode().getClient();
	}


	@Override
	public default String getTableName(){
		return getBackingNode().getTableName();
	}


	@Override
	public default String getPackagedTableName(){
		return getBackingNode().getPackagedTableName();
	}
}
