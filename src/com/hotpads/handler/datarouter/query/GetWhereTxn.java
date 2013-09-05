package com.hotpads.handler.datarouter.query;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.app.util.ResultMergeTool;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class GetWhereTxn<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends HibernateReaderNode<PK,D,F>>
extends BaseParallelHibernateTxnApp<List<D>>{

	private N node;
	private String tableName;
	private String where;
	
	public GetWhereTxn(DataRouterContext drContext, N node, String tableName, String where){
		super(node.getDataRouterContext(), node.getClientNames());
		this.tableName = tableName;
		this.where = where;
	}

	@Override
	public List<D> mergeResults(List<D> fromOnce, Collection<List<D>> fromEachClient){
		return ResultMergeTool.mergeIntoListAndSort(fromOnce, fromEachClient);
	}
	
	@Override
	public List<D> runOncePerClient(Client client){
		String sql = SqlBuilder.getAll(null, tableName, null, where, null);
		Session session = getSession(client.getName());
		return JdbcTool.selectDatabeans(session, node.getFieldInfo(), sql);
	}
	
}
