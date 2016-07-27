package com.hotpads.joblet.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.util.SqlBuilder;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.ParallelJobletProcessor;

@Singleton
public class JobletRequestSqlBuilder{

	private static final StringEnumField<JobletStatus> STATUS_CREATED_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.created);
	private static final StringEnumField<JobletStatus> STATUS_RUNNING_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.running);
	private static final BooleanField RESTARTABLE_FIELD = new BooleanField(JobletRequest.FieldKeys.restartable, true);


	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JdbcFieldCodecFactory jdbcFieldCodecFactory;


	//select for GetJobletRequest
	public String makeSelectFromClause(String tableName){
		StringBuilder sql = new StringBuilder();
		SqlBuilder.addSelectFromClause(sql, tableName, jobletNodes.jobletRequest().getFieldInfo().getFields());
		return sql.toString();
	}

	//update for ReserveJobletRequest
	public String makeUpdateClause(String tableName, String reservedBy){
		StringField reservedByField = new StringField(JobletRequest.FieldKeys.reservedBy, reservedBy);
		String setClause = " set " + jdbcFieldCodecFactory.createCodec(STATUS_CREATED_FIELD)
				.getSqlNameValuePairEscaped();
		setClause += ", " + jdbcFieldCodecFactory.createCodec(reservedByField).getSqlNameValuePairEscaped();
		return "update " + tableName + setClause;
	}

	//where for both
	public String makeWhereClause(JobletType<?> jobletType){
		StringBuilder sql = new StringBuilder();
		return sql
				.append(" where ")
				.append(makeTypeClause(jobletType))
				.append(" and ")
				.append(makeReadyClause())
				.append(" limit 1")
				.toString();
	}

	private String makeTypeClause(JobletType<?> jobletType){
		StringField typeField = new StringField(JobletRequestKey.FieldKeys.type, jobletType.getPersistentString());
		return jdbcFieldCodecFactory.createCodec(typeField).getSqlNameValuePairEscaped();
	}

	private String makeStatusCreatedClause(){
		return jdbcFieldCodecFactory.createCodec(STATUS_CREATED_FIELD).getSqlNameValuePairEscaped();
	}

	private String makeTimedOutClause(){
		return "(" + jdbcFieldCodecFactory.createCodec(STATUS_RUNNING_FIELD).getSqlNameValuePairEscaped() + " and "
				+ JobletRequest.FieldKeys.reservedAt.getColumnName() + " < " + computeReservedBeforeMs() + " and "
				+ jdbcFieldCodecFactory.createCodec(RESTARTABLE_FIELD).getSqlNameValuePairEscaped() + ")";
	}

	private String makeReadyClause(){
		return " (" + makeStatusCreatedClause() + " or " + makeTimedOutClause() + ") ";
	}

	private Long computeReservedBeforeMs(){
		return System.currentTimeMillis() - ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS;
	}
}
