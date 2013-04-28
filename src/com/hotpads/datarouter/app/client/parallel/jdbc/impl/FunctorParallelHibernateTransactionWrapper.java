package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.Functor;

public class FunctorParallelHibernateTransactionWrapper 
extends BaseParallelHibernateTxnApp<Integer>{

	private Functor<?,Client> func;

	public FunctorParallelHibernateTransactionWrapper(DataRouterContext drContext, List<String> clientNames,
			Isolation isolation, Functor<?,Client> func){
		super(drContext, clientNames, isolation);
		this.func = func;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		func.invoke(client);
		return 0;
	}

}