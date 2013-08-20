package com.hotpads.handler.datarouter.query;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.routing.DataRouterContext;

public class CountWhereTxn 
extends BaseParallelHibernateTxnApp<Long>{

	private String where;
	
	public CountWhereTxn(DataRouterContext drContext, List<String> clientNames, String where){
		super(drContext, clientNames);
	}

	@Override
	public Long mergeResults(
			Long fromOnce, 
			Collection<Long> fromEachClient) {
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long runOncePerClient(Client client){
		return null;
	}
	
}
