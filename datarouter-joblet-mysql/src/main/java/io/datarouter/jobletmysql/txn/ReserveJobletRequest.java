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

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.joblet.JobletRequestSqlBuilder;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;

public class ReserveJobletRequest extends BaseMysqlOp<Boolean>{

	private final ClientId clientId;
	private final String tableName;
	private final boolean disableIntroducer;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final JobletRequestSqlBuilder jobletRequestSqlBuilder;

	public ReserveJobletRequest(
			String reservedBy,
			JobletType<?> jobletType,
			Datarouter datarouter,
			DatarouterJobletRequestDao jobletRequestDao,
			MysqlSqlFactory mysqlSqlFactory,
			JobletRequestSqlBuilder jobletRequestSqlBuilder){
		super(datarouter,
				jobletRequestDao.getPhysicalNode().getClientId(),
				Isolation.repeatableRead,
				false);
		this.clientId = jobletRequestDao.getPhysicalNode().getClientId();
		this.tableName = jobletRequestDao.getPhysicalNode().getFieldInfo().getTableName();
		this.disableIntroducer = jobletRequestDao.getPhysicalNode().getFieldInfo().getDisableIntroducer();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.jobletRequestSqlBuilder = jobletRequestSqlBuilder;
	}

	@Override
	public Boolean runOnce(){
		MysqlSql sql = mysqlSqlFactory.createSql(clientId, tableName, disableIntroducer);
		jobletRequestSqlBuilder.makeReserveJobletRequest(sql, jobletType, reservedBy);
		int numRowsModified = MysqlTool.update(sql.prepare(getConnection()));
		return numRowsModified > 0;
	}

}
