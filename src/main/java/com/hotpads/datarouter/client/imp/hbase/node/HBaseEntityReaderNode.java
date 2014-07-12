package com.hotpads.datarouter.client.imp.hbase.node;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.node.entity.BaseEntityNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public abstract class HBaseEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BaseEntityNode<EK,E>{

	public HBaseEntityReaderNode(DataRouterContext drContext, HBaseTaskNameParams taskNameParams){
		super(drContext, taskNameParams);
	}

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getContext().getClientPool().getClient(getClientName());
	}
	
//	@Override
//	public E getEntity(EK key){
//		// TODO Auto-generated method stub
//		return null;
//	}
	
}
