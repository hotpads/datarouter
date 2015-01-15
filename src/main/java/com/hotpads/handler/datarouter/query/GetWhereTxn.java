package com.hotpads.handler.datarouter.query;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.StringTool;

public class GetWhereTxn<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends HibernateReaderNode<PK,D,F>>
extends BaseHibernateOp<List<D>>{

	private N node;
	private String tableName;
	private PK startAfterKey;
	private String whereClauseFromUser;
	private Config config;
	
	
	public GetWhereTxn(N node, String tableName, PK startAfterKey, String whereClauseFromUser, Config config){
		super(node.getDatarouterContext(), node.getClientNames());
		this.node = node;
		this.tableName = tableName;
		this.startAfterKey = startAfterKey;
		this.whereClauseFromUser = whereClauseFromUser;
		this.config = Config.nullSafe(config);
	}

	@Override
	public List<D> mergeResults(List<D> fromOnce, Collection<List<D>> fromEachClient){
		return ResultMergeTool.mergeIntoListAndSort(fromOnce, fromEachClient);
	}
	
	@Override
	public List<D> runOncePerClient(Client client){
		StringBuilder whereClause = new StringBuilder();
		if(startAfterKey != null){
			SqlBuilder.addRangeWhereClause(whereClause, startAfterKey, false, null, true);
			if(StringTool.notEmpty(whereClauseFromUser)){
				whereClause.append(" and ");
			}
		}
		whereClause.append(" "+whereClauseFromUser);
		String sql = SqlBuilder.getAll(config, tableName, node.getFieldInfo().getFields(), whereClause.toString(), 
				node.getFieldInfo().getPrimaryKeyFields());
		Session session = getSession(client.getName());
		return JdbcTool.selectDatabeans(session.connection(), node.getFieldInfo(), sql);
	}
	
}
