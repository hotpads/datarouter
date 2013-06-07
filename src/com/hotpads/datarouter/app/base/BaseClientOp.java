package com.hotpads.datarouter.app.base;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseClientOp<T>
extends BaseDataRouterOp<T>{

	private String clientName;
	
	public BaseClientOp(DataRouterContext drContext, String clientName){
		super(drContext);
		this.clientName = clientName;
	}

	public Client getClient(){
		return getDataRouterContext().getClientPool().getClient(clientName);
	}
	
}
