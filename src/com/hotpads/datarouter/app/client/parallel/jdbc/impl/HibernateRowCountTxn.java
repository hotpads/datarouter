package com.hotpads.datarouter.app.client.parallel.jdbc.impl;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;

public class HibernateRowCountTxn extends BaseParallelHibernateTxnApp<Integer>{

	private Class<?> bean;
	private String colToCount;
	private Criterion restriction;
	
	public HibernateRowCountTxn(DataRouterContext drContext, List<String> clientNames,
			Isolation isolation, Class<?> bean, String colToCount,
			Criterion restriction) {
		super(drContext, clientNames, isolation);
		this.bean = bean;
		this.colToCount = colToCount;
		this.restriction = restriction;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Integer runOncePerClient(Client client){
		Session session = getSession(client.getName());
		Criteria criteria = session.createCriteria(bean);
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.count(colToCount));
		criteria.setProjection(pl);
		if(restriction != null) criteria.add(restriction);

		List<Object> rows = criteria.list();

		if(rows == null || rows.size() < 1) return 0;
		return (Integer)rows.get(0);
	}
	
}