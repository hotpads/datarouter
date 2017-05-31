package com.hotpads.joblet.jdbc;

import java.sql.Connection;
import java.util.Collection;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.mysql.util.JdbcTool;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.type.JobletType;

public class GetJobletRequest extends BaseJdbcOp<JobletRequest>{

	private final String tableName;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final JdbcFieldCodecFactory mysqlFieldCodecFactory;
	private final JobletNodes jobletNodes;
	private final JobletRequestSqlBuilder sqlBuilder;

	public GetJobletRequest(String reservedBy, JobletType<?> jobletType,
			Datarouter datarouter, JobletNodes jobletNodes, JdbcFieldCodecFactory mysqlFieldCodecFactory,
			JobletRequestSqlBuilder sqlBuilder){
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.jobletNodes = jobletNodes;
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.mysqlFieldCodecFactory = mysqlFieldCodecFactory;
		this.sqlBuilder = sqlBuilder;
	}

	@Override
	public JobletRequest mergeResults(JobletRequest fromOnce, Collection<JobletRequest> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public JobletRequest runOncePerClient(Client client){
		Connection connection = getConnection(client.getName());

		String selectSql = makeSelectSql();
		JobletRequest jobletRequest = DrCollectionTool.getFirst(JdbcTool.selectDatabeans(mysqlFieldCodecFactory,
				connection, jobletNodes.jobletRequest().getFieldInfo(), selectSql));
		if(jobletRequest == null){
			return null;
		}

		//update the joblet
		if(jobletRequest.getStatus().isRunning()){
			//this was a timed out joblet. increment # timeouts
			jobletRequest.incrementNumTimeouts();
			if(jobletRequest.getNumTimeouts() > JobletService.MAX_JOBLET_RETRIES){
				//exceeded max retries. time out the joblet
				jobletRequest.setStatus(JobletStatus.timedOut);
			}
		}else{
			jobletRequest.setStatus(JobletStatus.running);
			jobletRequest.setReservedBy(reservedBy);
		}
		jobletNodes.jobletRequest().put(jobletRequest, null);

		return jobletRequest;
	}


	private String makeSelectSql(){
		StringBuilder sql = new StringBuilder();
		sql.append(sqlBuilder.makeSelectFromClause(tableName));
		sql.append(sqlBuilder.makeWhereClause(jobletType));
		sql.append(" for update");//lock the row
		return sql.toString();
	}

}