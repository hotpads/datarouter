package com.hotpads.datarouter.client.imp.jdbc.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcReaderNode;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class GetWhereTxn<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends JdbcReaderNode<PK,D,F>>
extends BaseJdbcOp<List<D>>{

	private N node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private String tableName;
	private PK startAfterKey;
	private String whereClauseFromUser;
	private Config config;
	
	
	public GetWhereTxn(N node, JdbcFieldCodecFactory fieldCodecFactory, String tableName, PK startAfterKey,
			String whereClauseFromUser, Config config){
		super(node.getDatarouter(), node.getClientNames());
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
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
			SqlBuilder.addRangeWhereClause(fieldCodecFactory, whereClause, startAfterKey, false, null, true);
			if(DrStringTool.notEmpty(whereClauseFromUser)){
				whereClause.append(" and ");
			}
		}
		whereClause.append(" "+whereClauseFromUser);
		String sql = SqlBuilder.getAll(config, tableName, node.getFieldInfo().getFields(), whereClause.toString(), 
				node.getFieldInfo().getPrimaryKeyFields());
		return JdbcTool.selectDatabeans(fieldCodecFactory, getConnection(client.getName()), node.getFieldInfo(), sql);
	}
	
}
