package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouter;

@Deprecated
public class FindByCriteria<K> extends BaseParallelHibernateTxnApp<List<K>>{
	Logger logger = Logger.getLogger(getClass());

	private Class<K> bean;
	private Criterion restriction;
	private Node<?,?> node;
	
	
	public FindByCriteria(Class<K> bean,
			Criterion restriction, DataRouter router, Node<?,?> node) {
		super(router);
		this.bean = bean;
		this.node = node;
		this.restriction = restriction;
		logger.warn("You probably shouldn't be using this method");
	}

	@Override
	public List<String> getClientNames() {
		return node.getClientNames();
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