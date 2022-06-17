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
package io.datarouter.client.mysql.op.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.string.StringTool;

public class MysqlVacuumTallyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Void>{
	private static final Logger logger = LoggerFactory.getLogger(MysqlVacuumTallyOp.class);

	private static final String ID = TallyKey.FieldKeys.id.getColumnName();

	private final MysqlSqlFactory mysqlSqlFactory;
	private final PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo;
	private final Config config;

	public MysqlVacuumTallyOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			Config config){
		super(datarouter, fieldInfo.getClientId());
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldInfo = fieldInfo;
		this.config = config;
	}

	@Override
	public Void runOnce(){
		Connection connection = getConnection();
		String tableName = fieldInfo.getTableName();
		boolean disableIntroducer = fieldInfo.getDisableIntroducer();
		int offset = config.findResponseBatchSize().orElse(10_000);
		Long nowMs = System.currentTimeMillis();
		try{
			MysqlSql endSql = buildSqlSelectKey(null, tableName, disableIntroducer, true, offset);
			PreparedStatement statementEnd = endSql.prepare(connection);
			statementEnd.execute();
			String startId = null;
			String endId = null;
			while(statementEnd.getResultSet().next()){
				endId = statementEnd.getResultSet().getString(ID);
			}
			while(startId != null || endId != null){
				MysqlSql sqlDelete = buildSqlDeleteRange(tableName, disableIntroducer, nowMs, startId, endId);
				PreparedStatement del = sqlDelete.prepare(connection);
				del.execute();
				startId = endId;
				endSql = buildSqlSelectKey(startId, tableName, disableIntroducer, false, offset);
				endId = null;
				if(endSql == null){
					continue;
				}
				statementEnd = endSql.prepare(connection);
				statementEnd.execute();
				while(statementEnd.getResultSet().next()){
					endId = statementEnd.getResultSet().getString(ID);
				}
			}
		}catch(SQLException e){
			logger.error("error with vacuum: ", e);
			throw new RuntimeException(e);
		}
		return null;
	}

	private MysqlSql buildSqlSelectKey(
			String startKey,
			String tableName,
			boolean disableIntroducer,
			boolean first,
			int offset){
		MysqlSql sql = mysqlSqlFactory
				.createSql(getClientId(), tableName, disableIntroducer)
				.addSelectFromClause(tableName, fieldInfo.getPrimaryKeyFields());
		if(!first && startKey == null){
			return null;
		}

		if(!first){
			sql.append(" where ");
			sql.append(ID);
			sql.append(">=");
			sql.append(StringTool.escapeString(startKey));
		}
		sql.addLimitOffsetClause(new Config().setLimit(1).setOffset(offset));
		return sql;
	}

	private MysqlSql buildSqlDeleteRange(
			String tableName,
			boolean disableIntroducer,
			Long nowMs,
			String startId,
			String endId){
		MysqlSql sqlDelete = mysqlSqlFactory
				.createSql(getClientId(), tableName, disableIntroducer)
				.addDeleteFromClause(tableName)
				.append(" where ");
		if(startId != null){
			sqlDelete
				.append(ID)
				.append(" >= ")
				.append(StringTool.escapeString(startId))
				.append(" and ");
		}
		if(endId != null){
			sqlDelete
				.append(ID)
				.append(" < ")
				.append(StringTool.escapeString(endId))
				.append(" and ");
		}
		sqlDelete
				.append(Tally.FieldKeys.expirationMs.getColumnName())
				.append(" < ")
				.append("" + nowMs);
		return sqlDelete;
	}

}
