package com.hotpads.datarouter.client.imp.jdbc.op.custom;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.util.core.Functor;

public class FunctorParallelTransactionWrapper extends BaseJdbcOp<Integer>{

	private final Functor<?,Client> func;

	public FunctorParallelTransactionWrapper(Datarouter datarouter, List<String> clientNames,
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