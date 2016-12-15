package com.hotpads.joblet.jdbc;

import java.sql.Connection;
import java.util.Collection;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.type.JobletType;

public class ReserveJobletRequest extends BaseJdbcOp<Boolean>{

	private final String tableName;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final JobletRequestSqlBuilder sqlBuilder;

	public ReserveJobletRequest(String reservedBy, JobletType<?> jobletType, Datarouter datarouter,
			JobletNodes jobletNodes, JobletRequestSqlBuilder sqlBuilder){
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.sqlBuilder = sqlBuilder;
	}

	@Override
	public Boolean mergeResults(Boolean fromOnce, Collection<Boolean> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Boolean runOncePerClient(Client client){
		Connection connection = getConnection(client.getName());
		StringBuilder sql = new StringBuilder();
		sql.append(sqlBuilder.makeUpdateClause(tableName, reservedBy));
		sql.append(sqlBuilder.makeWhereClause(jobletType));
		int numRowsModified = JdbcTool.update(connection, sql.toString());
		return numRowsModified > 0;
	}

}