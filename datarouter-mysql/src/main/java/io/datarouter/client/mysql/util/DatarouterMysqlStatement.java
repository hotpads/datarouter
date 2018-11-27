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
package io.datarouter.client.mysql.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import io.datarouter.model.exception.DataAccessException;

public class DatarouterMysqlStatement{

	private final StringBuilder sqlBuilder;
	private final ArrayList<DatarouterMysqlStatementParameterizer> parameterizers;

	public DatarouterMysqlStatement(){
		this.sqlBuilder = new StringBuilder();
		this.parameterizers = new ArrayList<>();
	}

	public DatarouterMysqlStatement append(String someMoreSql){
		sqlBuilder.append(someMoreSql);
		return this;
	}

	public DatarouterMysqlStatement append(String someMoreSql, DatarouterMysqlStatementParameterizer parameterizer){
		append(someMoreSql);
		parameterizers.add(parameterizer);
		return this;
	}

	public PreparedStatement toPreparedStatement(StatementPreparer statementPreparer){
		String sql = sqlBuilder.toString();
		PreparedStatement statement;
		try{
			statement = statementPreparer.prepareStatement(sql);
		}catch(Exception e){
			throw new DataAccessException("error preparing statement with sql:" + sql, e);
		}
		for(int i = 0; i < parameterizers.size(); i++){
			parameterizers.get(i).parameterize(statement, i + 1);
		}
		return statement;
	}

	public PreparedStatement toPreparedStatement(Connection connection){
		return toPreparedStatement(connection::prepareStatement);
	}

	public StringBuilder getSql(){
		return sqlBuilder;
	}

	public interface StatementPreparer{
		PreparedStatement prepareStatement(String sql) throws Exception;
	}

}
