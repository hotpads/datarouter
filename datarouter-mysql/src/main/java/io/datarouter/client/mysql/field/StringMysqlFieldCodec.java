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
package io.datarouter.client.mysql.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.CharSequenceSqlColumn;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.StringEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.StringField;

public class StringMysqlFieldCodec
extends BaseMysqlFieldCodec<String,StringField>{

	public StringMysqlFieldCodec(StringField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		return new CharSequenceSqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(),
				StringEncodedMysqlFieldCodec.getNormalizedSize(field.getSize()),
				nullable,
				false,
				field.getKey().getDefaultValue(),
				StringEncodedMysqlFieldCodec.DEFAULT_CHARACTER_SET,
				StringEncodedMysqlFieldCodec.DEFAULT_COLLATION);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public String fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			return rs.getString(field.getKey().getColumnName());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return StringEncodedMysqlFieldCodec.getColumnType(field.getSize());
	}

}
