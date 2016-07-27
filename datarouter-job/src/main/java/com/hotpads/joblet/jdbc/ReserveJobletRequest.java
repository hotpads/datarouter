package com.hotpads.joblet.jdbc;

import java.sql.Connection;
import java.util.Collection;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.ParallelJobletProcessor;

public class ReserveJobletRequest extends BaseJdbcOp<JobletRequest>{

	public static final int MAX_JOBLET_RETRIES = 10;

	private static final StringEnumField<JobletStatus> STATUS_CREATED_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.created);
	private static final StringEnumField<JobletStatus> STATUS_RUNNING_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.running);
	private static final BooleanField RESTARTABLE_FIELD = new BooleanField(JobletRequest.FieldKeys.restartable, true);

	private final String tableName;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final JdbcFieldCodecFactory jdbcFieldCodecFactory;
	private final JobletNodes jobletNodes;

	public ReserveJobletRequest(String reservedBy, JobletType<?> jobletType,
			Datarouter datarouter, JobletNodes jobletNodes, JdbcFieldCodecFactory jdbcFieldCodecFactory){
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.jobletNodes = jobletNodes;
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.jdbcFieldCodecFactory = jdbcFieldCodecFactory;
	}

	@Override
	public JobletRequest mergeResults(JobletRequest fromOnce, Collection<JobletRequest> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public JobletRequest runOncePerClient(Client client){
		Connection connection = getConnection(client.getName());
		JobletRequest jobletRequest;
		do{
			jobletRequest = getOneJoblet(connection);
		}while(jobletRequest != null && !jobletRequest.getStatus().isRunning());
		return jobletRequest;
	}

	public JobletRequest getOneJoblet(Connection connection){
		StringField typeField = new StringField(JobletRequestKey.FieldKeys.type, jobletType.getPersistentString());

		String typeClause = jdbcFieldCodecFactory.createCodec(typeField).getSqlNameValuePairEscaped();
		String statusCreatedClause = jdbcFieldCodecFactory.createCodec(STATUS_CREATED_FIELD)
				.getSqlNameValuePairEscaped();
		String timedOutClause = "(" + jdbcFieldCodecFactory.createCodec(STATUS_RUNNING_FIELD)
				.getSqlNameValuePairEscaped() + " and " + JobletRequest.FieldKeys.reservedAt.getColumnName() + " < "
				+ computeReservedBeforeMs() + " and " + jdbcFieldCodecFactory.createCodec(RESTARTABLE_FIELD)
				.getSqlNameValuePairEscaped() + ")";

		String readyClause = " (" + statusCreatedClause + " or " + timedOutClause + ") ";

		StringBuilder reserveSql = new StringBuilder();
		SqlBuilder.addSelectFromClause(reserveSql, tableName, jobletNodes.jobletRequest().getFieldInfo().getFields());
		reserveSql.append(" where ").append(typeClause).append(" and ").append(readyClause)
				.append(" limit 1 for update");

		JobletRequest jobletRequest = DrCollectionTool.getFirst(JdbcTool.selectDatabeans(jdbcFieldCodecFactory,
				connection, jobletNodes.jobletRequest().getFieldInfo(), reserveSql.toString()));
		if(jobletRequest == null) {
			return null;
		}

		//update the joblet
		if(jobletRequest.getStatus().isRunning()){
			//this was a timed out joblet. increment # timeouts
			jobletRequest.setNumTimeouts(DrNumberTool.nullSafe(jobletRequest.getNumTimeouts())+1);
			if(jobletRequest.getNumTimeouts() > MAX_JOBLET_RETRIES){
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

	private Long computeReservedBeforeMs(){
		return System.currentTimeMillis() - ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS;
	}

}