package com.hotpads.datarouter.client.imp.hibernate.op.custom;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.util.core.Functor;

public class FunctorParallelHibernateTransactionWrapper 
extends BaseHibernateOp<Integer>{

	private final Functor<?,Client> func;

	public FunctorParallelHibernateTransactionWrapper(Datarouter datarouter, List<String> clientNames,
			Functor<?,Client> func){
		super(datarouter, clientNames);
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