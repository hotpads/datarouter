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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;


public class MysqlIncrementOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Long>{
	private static final Logger logger = LoggerFactory.getLogger(MysqlIncrementOp.class);

	private final PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final String key;
	private final Long incrementAmount;

	public MysqlIncrementOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo,
			MysqlFieldCodecFactory fieldCodecFactory,
			MysqlSqlFactory mysqlSqlFactory,
			String key,
			Long incrementAmount){
		super(datarouter, fieldInfo.getClientId(), Isolation.readCommitted, false);
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.key = key;
		this.incrementAmount = incrementAmount;
	}

	@Override
	public Long runOnce(){
		Connection connection = getConnection();
		Tally databean = new Tally(key, incrementAmount);
		List<Field<?>> fields = fieldInfo.getFieldsWithValues(
				databean);
		var incrementSql = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName(), fieldInfo.getDisableIntroducer())
				.incrementTally(fieldInfo.getTableName(), key, incrementAmount);
		var selectSql = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName(), fieldInfo.getDisableIntroducer())
				.addSelectFromClause(fieldInfo.getTableName(), fields);
		PreparedStatement incrementStatement = incrementSql.prepare(connection);
		PreparedStatement selectStatement = selectSql.prepare(connection);
		try{
			incrementStatement.execute();
			selectStatement.execute();
			connection.commit();
			Tally selectBean = MysqlTool.getDatabeansFromSelectResult(
					fieldCodecFactory,
					fieldInfo.getDatabeanSupplier(),
					fieldInfo.getFields(),
					selectStatement)
				.get(0);
			return selectBean.getTally();
		}catch(SQLException e){
			try{
				connection.rollback();
			}catch(SQLException exception){
				logger.error("error with rollbacking sql. exception {}", exception);
			}
		}
		return null;
	}

}