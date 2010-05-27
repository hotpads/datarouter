package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.Functor;

public class FunctorParallelHibernateTransactionWrapper 
extends BaseParallelHibernateTxnApp<Integer>{
	Logger logger = Logger.getLogger(getClass());

	private Functor<?,Void> func;
	private Node<?,?> node;
	

	public FunctorParallelHibernateTransactionWrapper(
			Functor<?,Void> func, DataRouter router, Node<?,?> node) {
		super(router);
		this.node = node;
		this.func = func;
	}

	@Override
	public List<String> getClientNames() {
		return node.getClientNames();
	}

	@Override
	public Integer mergeResults(
			Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}
	
	@Override
	public Integer runOncePerClient(Client client){
		
		func.invoke(null);
			
		return 0;
	}

}