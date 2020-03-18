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

import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
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

	private final ClientId clientId;
	private final String tableName;
	private final MysqlSqlFactory mysqlSqlFactory;

	@Inject
	public JobletRequestSqlBuilder(
			DatarouterJobletRequestDao jobletRequestDao,
			MysqlSqlFactory mysqlSqlFactory){
		this.jobletRequestDao = jobletRequestDao;
		PhysicalNode<?,?,?> physicalNode = NodeTool.extractSinglePhysicalNode(jobletRequestDao.getNode());
		this.clientId = physicalNode.getClientId();
		this.tableName = physicalNode.getFieldInfo().getTableName();
		this.mysqlSqlFactory = mysqlSqlFactory;
	}

	//select for GetJobletRequest
	public MysqlSql makeSelectFromClause(){
		return mysqlSqlFactory.createSql(clientId, tableName)
				.addSelectFromClause(tableName, jobletRequestDao.getNode().getFieldInfo().getFields());
	}

	//update for ReserveJobletRequest
	public MysqlSql makeUpdateClause(String reservedBy){
		StringField reservedByField = new StringField(JobletRequest.FieldKeys.reservedBy, reservedBy);
		return mysqlSqlFactory.createSql(clientId, tableName)
				.addUpdateClause(tableName)
				.appendSqlNameValue(reservedByField, true);
	}

	//where for both
	public void appendWhereClause(MysqlSql sql, JobletType<?> jobletType){
		sql.append(" where ");
		appendTypeClause(sql, jobletType);
		sql.append(" and ");
		appendStatusClause(sql);
		sql.append(" limit 1");
	}

	private void appendTypeClause(MysqlSql sql, JobletType<?> jobletType){
		StringField typeField = new StringField(JobletRequestKey.FieldKeys.type, jobletType.getPersistentString());
		sql.appendSqlNameValue(typeField, true);
	}

	private void appendStatusClause(MysqlSql sql){
		sql.append(" (")
				.appendSqlNameValue(STATUS_CREATED_FIELD, true)
				.append(" or ");
		makeStatusTimedOutClause(sql);
		sql.append(") ");
	}

	private void makeStatusTimedOutClause(MysqlSql sql){
		sql.append("(")
				.appendSqlNameValue(STATUS_RUNNING_FIELD, true)
				.append(" and ")
				.append(JobletRequest.FieldKeys.reservedAt.getColumnName())
				.append(" < ")
				.append(computeReservedBeforeMs().toString())
				.append(" and ")
				.appendSqlNameValue(RESTARTABLE_FIELD, true)
				.append(")");
	}

	private Long computeReservedBeforeMs(){
		return System.currentTimeMillis() - DatarouterJobletConstants.RUNNING_JOBLET_TIMEOUT_MS;
	}

}
