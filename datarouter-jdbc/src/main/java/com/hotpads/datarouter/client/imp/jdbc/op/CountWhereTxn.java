package com.hotpads.datarouter.client.imp.jdbc.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.core.DrStringTool;

public class CountWhereTxn 
extends BaseJdbcOp<Long>{

	private String tableName;
	private String where;
	
	public CountWhereTxn(DatarouterContext drContext, List<String> clientNames, String tableName, String where){
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
		if(DrStringTool.notEmpty(where)){
			sql += " where " + where;
		}
		return JdbcTool.count(getConnection(client.getName()), sql);
	}
	
}