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
import java.sql.PreparedStatement;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.DatarouterMysqlStatement;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest.JobletRequestFielder;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;

public class GetJobletRequest extends BaseMysqlOp<JobletRequest>{

	private final PhysicalDatabeanFieldInfo<JobletRequestKey,JobletRequest,JobletRequestFielder> fieldInfo;
	private final String reservedBy;
	private final JobletType<?> jobletType;

	private final DatarouterJobletRequestDao jobletRequestDao;
	private final MysqlFieldCodecFactory mysqlFieldCodecFactory;
	private final JobletRequestSqlBuilder sqlBuilder;

	public GetJobletRequest(
			String reservedBy,
			JobletType<?> jobletType,
			Datarouter datarouter,
			DatarouterJobletRequestDao jobletRequestDao,
			MysqlFieldCodecFactory mysqlFieldCodecFactory,
			JobletRequestSqlBuilder sqlBuilder){
		super(datarouter, NodeTool.extractSinglePhysicalNode(jobletRequestDao.getNode()).getClientId(),
				Isolation.repeatableRead, false);
		this.fieldInfo = NodeTool.extractSinglePhysicalNode(jobletRequestDao.getNode()).getFieldInfo();
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;

		this.jobletRequestDao = jobletRequestDao;
		this.mysqlFieldCodecFactory = mysqlFieldCodecFactory;
		this.sqlBuilder = sqlBuilder;
	}

	@Override
	public JobletRequest runOnce(){
		Connection connection = getConnection();

		PreparedStatement selectStatement = makeSelectStatement().toPreparedStatement(connection);
		JobletRequest jobletRequest = CollectionTool.getFirst(MysqlTool.selectDatabeans(mysqlFieldCodecFactory,
				fieldInfo.getDatabeanSupplier(), fieldInfo.getFields(), selectStatement));
		if(jobletRequest == null){
			return null;
		}

		//update the joblet
		if(jobletRequest.getStatus().isRunning()){
			//this was a timed out joblet. increment # timeouts
			jobletRequest.incrementNumTimeouts();
			if(jobletRequest.getNumTimeouts() > JobletService.MAX_JOBLET_RETRIES){
				//exceeded max retries. time out the joblet
				jobletRequest.setStatus(JobletStatus.TIMED_OUT);
			}
		}else{
			jobletRequest.setStatus(JobletStatus.RUNNING);
			jobletRequest.setReservedBy(reservedBy);
			jobletRequest.setReservedAt(System.currentTimeMillis());
		}
		jobletRequestDao.put(jobletRequest);

		return jobletRequest;
	}

	private DatarouterMysqlStatement makeSelectStatement(){
		DatarouterMysqlStatement statement = sqlBuilder.makeSelectFromClause();
		sqlBuilder.appendWhereClause(statement, jobletType);
		statement.append(" for update");//lock the row
		return statement;
	}

}
