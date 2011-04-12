package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouter;

public class HibernateRowCountTxn extends BaseParallelHibernateTxnApp<Integer>{
	Logger logger = Logger.getLogger(getClass());

	private Class<?> bean;
	private String colToCount;
	private Criterion restriction;
	private Node<?,?> node;
	
	public HibernateRowCountTxn(Class<?> bean, String colToCount, 
			DataRouter router, Node<?,?> node) {
		this(bean,colToCount,null,router,node);
	}
	public HibernateRowCountTxn(Class<?> bean, String colToCount,
			Criterion restriction, DataRouter router, Node<?,?> node) {
		super(router);
		this.bean = bean;
		this.colToCount = colToCount;
		this.node = node;
		this.restriction = restriction;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Integer runOncePerClient(Client client){
		Session session = this.getSession(client.getName());
		Criteria criteria = session.createCriteria(bean);
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.count(colToCount));
		criteria.setProjection(pl);
		if(restriction!=null) criteria.add(restriction);
		
		List<Object> rows = criteria.list();
		
		if(rows==null || rows.size()<1) return 0;
		return (Integer)rows.get(0);
	}
	
	
}