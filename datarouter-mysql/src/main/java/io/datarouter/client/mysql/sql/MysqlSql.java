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
package io.datarouter.client.mysql.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;

import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.sql.Sql;

public class MysqlSql extends Sql<Connection,PreparedStatement,MysqlSql>{

	private final MysqlFieldCodecFactory codecFactory;
	private final MysqlLiveTableOptions mysqlLiveTableOptions;
	private final boolean disableIntroducer;

	public MysqlSql(
			MysqlFieldCodecFactory codecFactory,
			MysqlLiveTableOptions mysqlTableOptions,
			boolean disableIntroducer){
		super(MysqlSql.class);
		this.codecFactory = codecFactory;
		this.mysqlLiveTableOptions = mysqlTableOptions;
		this.disableIntroducer = disableIntroducer;
	}

	@Override
	public <T,F extends Field<T>> MysqlSql appendColumnEqualsValueParameter(F field){
		MysqlFieldCodec<T,F> codec = codecFactory.createCodec(field);
		appendParameter(codec.getSqlParameter(), (ps, index) -> codec.setPreparedStatementValue(ps, index, field));
		return this;
	}

	@Override
	public <T,F extends Field<T>> MysqlSql addSqlNameValueWithOperator(
			F field,
			String operator,
			boolean rejectNulls){
		if(rejectNulls && field.getValue() == null){
			throw new RuntimeException(field.getKey().getColumnName() + " should not be null, current sql is "
					+ sqlBuilder);
		}
		append(field.getKey().getColumnName());
		append(operator);
		MysqlFieldCodec<T,F> codec = codecFactory.createCodec(field);
		String parameter = field.getValue() == null
				? codec.getSqlParameter()
				: codec.getIntroducedParameter(mysqlLiveTableOptions, disableIntroducer, field);
		appendParameter(parameter, (ps, index) -> codec.setPreparedStatementValue(ps, index, field));
		return this;
	}

	@Override
	public PreparedStatement prepare(Connection connection){
		return prepareMysql(connection::prepareStatement);
	}

	public PreparedStatement prepareMysql(MysqlStatementPreparer mysqlStatementPreparer){
		String sqlString = sqlBuilder.toString();
		PreparedStatement statement;
		try{
			statement = mysqlStatementPreparer.prepareStatement(sqlString);
		}catch(Exception e){
			throw new DataAccessException("error preparing statement with sql=" + sqlBuilder, e);
		}
		for(int i = 0; i < parameterSetters.size(); i++){
			parameterSetters.get(i).accept(statement, i + 1);
		}
		return statement;
	}

	@Override
	public MysqlSql addLimitOffsetClause(Config config){
		if(config.getLimit() != null && config.getOffset() != null){
			append(" limit " + config.getOffset() + ", " + config.getLimit());
		}else if(config.getLimit() != null){
			append(" limit " + config.getLimit());
		}else if(config.getOffset() != null){
			append(" limit " + config.getOffset() + ", " + Integer.MAX_VALUE);// stupid mysql syntax
		}
		return this;
	}

}
