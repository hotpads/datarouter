package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouterContext;

@Deprecated
public class FindByCriteria<K> extends BaseParallelHibernateTxnApp<List<K>>{

	private Class<K> bean;
	private Criterion restriction;
	
	
	public FindByCriteria(DataRouterContext drContext, List<String> clientNames, Isolation isolation, 
			Class<K> bean, Criterion restriction, Node<?,?> node) {
		super(drContext, clientNames, isolation);
		this.bean = bean;
		this.restriction = restriction;
		getLogger().warn("You probably shouldn't be using this method");
	}

	@Override
	public List<K> mergeResults(List<K> fromOnce, Collection<List<K>> fromEachClient) {
		return ResultMergeTool.append(fromOnce, fromEachClient);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<K> runOncePerClient(Client client){
		Session session = this.getSession(client.getName());
		Criteria criteria = session.createCriteria(bean);
		if(restriction!=null) criteria.add(restriction);
		
		List<K> rows = criteria.list();
		
		return rows;
	}	
	
}