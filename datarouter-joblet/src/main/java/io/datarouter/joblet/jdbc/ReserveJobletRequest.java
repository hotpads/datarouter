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

import java.sql.Connection;

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.DatarouterMysqlStatement;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.NodeTool;

public class ReserveJobletRequest extends BaseMysqlOp<Boolean>{

	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final JobletRequestSqlBuilder sqlBuilder;

	public ReserveJobletRequest(
			String reservedBy,
			JobletType<?> jobletType,
			Datarouter datarouter,
			DatarouterJobletRequestDao jobletRequestDao,
			JobletRequestSqlBuilder sqlBuilder){
		super(datarouter, NodeTool.extractSinglePhysicalNode(jobletRequestDao.getNode()).getClientId(),
				Isolation.repeatableRead, false);
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.sqlBuilder = sqlBuilder;
	}

	@Override
	public Boolean runOnce(){
		Connection connection = getConnection();
		DatarouterMysqlStatement statement = sqlBuilder.makeUpdateClause(reservedBy);
		sqlBuilder.appendWhereClause(statement, jobletType);
		int numRowsModified = MysqlTool.update(statement.toPreparedStatement(connection));
		return numRowsModified > 0;
	}

}
