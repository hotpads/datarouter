/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptionsRefresher;
import io.datarouter.client.mysql.util.DatarouterMysqlStatement;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.SqlBuilder;
import io.datarouter.joblet.DatarouterJobletConstants;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class JobletRequestSqlBuilder{

	private static final StringEnumField<JobletStatus> STATUS_CREATED_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.CREATED);
	private static final StringEnumField<JobletStatus> STATUS_RUNNING_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.RUNNING);
	private static final BooleanField RESTARTABLE_FIELD = new BooleanField(JobletRequest.FieldKeys.restartable, true);


	private final DatarouterJobletRequestDao jobletRequestDao;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher;

	private final ClientId clientId;
	private final String tableName;

	@Inject
	public JobletRequestSqlBuilder(
			DatarouterJobletRequestDao jobletRequestDao,
			MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher){
		this.jobletRequestDao = jobletRequestDao;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.mysqlLiveTableOptionsRefresher = mysqlLiveTableOptionsRefresher;

		PhysicalNode<?,?,?> physicalNode = NodeTool.extractSinglePhysicalNode(jobletRequestDao.getNode());
		this.clientId = physicalNode.getClientId();
		this.tableName = physicalNode.getFieldInfo().getTableName();
	}

	//select for GetJobletRequest
	public DatarouterMysqlStatement makeSelectFromClause(){
		return mysqlPreparedStatementBuilder.select(tableName, jobletRequestDao.getNode().getFieldInfo().getFields());
	}

	//update for ReserveJobletRequest
	public DatarouterMysqlStatement makeUpdateClause(String reservedBy){
		DatarouterMysqlStatement statement = new DatarouterMysqlStatement();
		StringField reservedByField = new StringField(JobletRequest.FieldKeys.reservedBy, reservedBy);
		SqlBuilder.addUpdateClause(statement.getSql(), tableName);
		mysqlPreparedStatementBuilder.appendSqlNameValue(statement, reservedByField,
				mysqlLiveTableOptionsRefresher.get(clientId, reservedBy));
		return statement;
	}

	//where for both
	public void appendWhereClause(DatarouterMysqlStatement statement, JobletType<?> jobletType){
		statement.append(" where ");
		appendTypeClause(statement, jobletType);
		statement.append(" and ");
		appendStatusClause(statement);
		statement.append(" limit 1");
	}

	private void appendTypeClause(DatarouterMysqlStatement statement, JobletType<?> jobletType){
		StringField typeField = new StringField(JobletRequestKey.FieldKeys.type, jobletType.getPersistentString());
		mysqlPreparedStatementBuilder.appendSqlNameValue(statement, typeField,
				mysqlLiveTableOptionsRefresher.get(clientId, tableName));
	}

	private void appendStatusClause(DatarouterMysqlStatement statement){
		statement.append(" (");
		mysqlPreparedStatementBuilder.appendSqlNameValue(statement, STATUS_CREATED_FIELD,
				mysqlLiveTableOptionsRefresher.get(clientId, tableName));
		statement.append(" or ");
		makeStatusTimedOutClause(statement);
		statement.append(") ");
	}

	private void makeStatusTimedOutClause(DatarouterMysqlStatement statement){
		statement.append("(");
		mysqlPreparedStatementBuilder.appendSqlNameValue(statement, STATUS_RUNNING_FIELD,
				mysqlLiveTableOptionsRefresher.get(clientId, tableName));
		statement.append(" and ").append(JobletRequest.FieldKeys.reservedAt.getColumnName()).append(" < ")
				.append(computeReservedBeforeMs().toString()).append(" and ");
		mysqlPreparedStatementBuilder.appendSqlNameValue(statement, RESTARTABLE_FIELD,
				mysqlLiveTableOptionsRefresher.get(clientId, tableName));
		statement.append(")");
	}

	private Long computeReservedBeforeMs(){
		return System.currentTimeMillis() - DatarouterJobletConstants.RUNNING_JOBLET_TIMEOUT_MS;
	}

}
