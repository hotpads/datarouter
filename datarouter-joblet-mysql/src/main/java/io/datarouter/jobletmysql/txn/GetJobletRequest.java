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
package io.datarouter.jobletmysql.txn;

import java.sql.PreparedStatement;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.joblet.JobletRequestSqlBuilder;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest.JobletRequestFielder;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class GetJobletRequest extends BaseMysqlOp<JobletRequest>{

	private final PhysicalDatabeanFieldInfo<JobletRequestKey,JobletRequest,JobletRequestFielder> fieldInfo;
	private final String reservedBy;
	private final JobletType<?> jobletType;

	private final MysqlFieldCodecFactory mysqlFieldCodecFactory;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final JobletRequestSqlBuilder jobletRequestSqlBuilder;
	private final JobletService jobletService;

	public GetJobletRequest(
			String reservedBy,
			JobletType<?> jobletType,
			DatarouterClients datarouterClients,
			DatarouterJobletRequestDao jobletRequestDao,
			MysqlFieldCodecFactory mysqlFieldCodecFactory,
			MysqlSqlFactory mysqlSqlFactory,
			JobletRequestSqlBuilder jobletRequestSqlBuilder,
			JobletService jobletService){
		super(datarouterClients,
				jobletRequestDao.getPhysicalNode().getClientId(),
				Isolation.repeatableRead,
				false);
		this.fieldInfo = jobletRequestDao.getPhysicalNode().getFieldInfo();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.mysqlFieldCodecFactory = mysqlFieldCodecFactory;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.jobletRequestSqlBuilder = jobletRequestSqlBuilder;
		this.jobletService = jobletService;
	}

	@Override
	public JobletRequest runOnce(){
		PreparedStatement selectStatement = makeSelectStatement().prepare(getConnection());
		JobletRequest jobletRequest = MysqlTool.selectDatabeans(
				mysqlFieldCodecFactory,
				fieldInfo.getDatabeanSupplier(),
				fieldInfo.getFields(),
				selectStatement).stream()
				.findFirst()
				.orElse(null);
		if(jobletRequest == null){
			return null;
		}
		jobletService.updateStatusToRunning(jobletRequest, reservedBy);
		return jobletRequest;
	}

	private MysqlSql makeSelectStatement(){
		MysqlSql sql = mysqlSqlFactory.createSql(
				fieldInfo.getClientId(),
				fieldInfo.getTableName(),
				fieldInfo.getDisableIntroducer());
		jobletRequestSqlBuilder.makeGetJobletRequest(sql, jobletType);
		return sql;
	}

}
