package com.hotpads.datarouter.client.imp.mysql.op.custom;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;

public class FunctorParallelTransactionWrapper extends BaseJdbcOp<Integer>{

	private final Function<Client,?> func;

	public FunctorParallelTransactionWrapper(Datarouter datarouter, List<String> clientNames, Function<Client,?> func){
		super(datarouter, clientNames);
		this.func = func;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		func.apply(client);
		return 0;
	}

}