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
package io.datarouter.client.mysql.field.codec.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.field.imp.comparable.LongEncodedField;

public class LongEncodedMysqlFieldCodec<T>
extends BaseMysqlFieldCodec<T,LongEncodedField<T>>{

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, LongEncodedField<T> field){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(field),
				20,
				allowNullable && field.getKey().isNullable(),
				FieldGeneratorType.MANAGED == field.getKey().getAutoGeneratedType());
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, LongEncodedField<T> field){
		Long longValue = field.getCodec().encode(field.getValue());
		try{
			if(longValue == null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, longValue);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public T fromMysqlResultSetButDoNotSet(ResultSet rs, LongEncodedField<T> field){
		try{
			long value = rs.getLong(field.getKey().getColumnName());
			if(rs.wasNull()){
				return field.getCodec().decode(null);
			}else{
				return field.getCodec().decode(value);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(LongEncodedField<T> field){
		return MysqlColumnType.BIGINT;
	}

}