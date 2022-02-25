/*
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
package io.datarouter.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.sql.Sql;

@Singleton
public class JobletRequestSqlBuilder{

	private static final StringEnumField<JobletStatus> STATUS_CREATED_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.CREATED);
	private static final StringEnumField<JobletStatus> STATUS_RUNNING_FIELD = new StringEnumField<>(
			JobletRequest.FieldKeys.status, JobletStatus.RUNNING);
	private static final BooleanField RESTARTABLE_FIELD = new BooleanField(JobletRequest.FieldKeys.restartable, true);

	private final DatarouterJobletSettingRoot datarouterJobletSettingRoot;
	private final DatarouterJobletRequestDao jobletRequestDao;

	private final String tableName;

	@Inject
	public JobletRequestSqlBuilder(
			DatarouterJobletSettingRoot datarouterJobletSettingRoot,
			DatarouterJobletRequestDao jobletRequestDao){
		this.datarouterJobletSettingRoot = datarouterJobletSettingRoot;
		this.jobletRequestDao = jobletRequestDao;
		PhysicalNode<?,?,?> physicalNode = jobletRequestDao.getPhysicalNode();
		this.tableName = physicalNode.getFieldInfo().getTableName();
	}

	public Sql<?,?,?> makeReserveJobletRequest(Sql<?,?,?> sql, JobletType<?> jobletType, String reservedBy){
		StringField reservedByField = new StringField(JobletRequest.FieldKeys.reservedBy, reservedBy);
		sql.addUpdateClause(tableName);
		sql.appendSqlNameValue(reservedByField, true);
		appendWhereClause(sql, jobletType);
		return sql;
	}

	public Sql<?,?,?> makeGetJobletRequest(Sql<?,?,?> sql, JobletType<?> jobletType){
		sql.addSelectFromClause(tableName, jobletRequestDao.getPhysicalNode().getFieldInfo().getFields());
		appendWhereClause(sql, jobletType);
		sql.append(" for update");//lock the row
		return sql;
	}

	private Sql<?,?,?> appendWhereClause(Sql<?,?,?> sql, JobletType<?> jobletType){
		sql.append(" where ");
		appendTypeClause(sql, jobletType);
		sql.append(" and ");
		appendStatusClause(sql);
		sql.append(" limit 1");
		return sql;
	}

	private void appendTypeClause(Sql<?,?,?> sql, JobletType<?> jobletType){
		StringField typeField = new StringField(JobletRequestKey.FieldKeys.type, jobletType.getPersistentString());
		sql.appendSqlNameValue(typeField, true);
	}

	private void appendStatusClause(Sql<?,?,?> sql){
		sql.append(" (")
				.appendSqlNameValue(STATUS_CREATED_FIELD, true)
				.append(" or ");
		appendStatusTimedOutClause(sql);
		sql.append(") ");
	}

	private void appendStatusTimedOutClause(Sql<?,?,?> sql){
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
		return System.currentTimeMillis() - datarouterJobletSettingRoot.jobletTimeout.get().toMillis();
	}

}
