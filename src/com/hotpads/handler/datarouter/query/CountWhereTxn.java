package com.hotpads.handler.datarouter.query;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.StringTool;

public class CountWhereTxn 
extends BaseHibernateOp<Long>{

	private String tableName;
	private String where;
	
	public CountWhereTxn(DataRouterContext drContext, List<String> clientNames, String tableName, String where){
		super(drContext, clientNames);
		this.tableName = tableName;
		this.where = where;
	}

	@Override
	public Long mergeResults(Long fromOnce, Collection<Long> fromEachClient){
		return ResultMergeTool.sum(fromOnce, fromEachClient);
	}
	
	@Override
	public Long runOncePerClient(Client client){
		String sql = "select count(*) from "+tableName;
		if(StringTool.notEmpty(where)){
			sql += " where " + where;
		}
		Session session = getSession(client.getName());
		return JdbcTool.count(session.connection(), sql);
	}
	
}
