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
import io.datarouter.model.field.imp.comparable.IntegerEncodedField;

public class IntegerEncodedMysqlFieldCodec<T>
extends BaseMysqlFieldCodec<T,IntegerEncodedField<T>>{

	public IntegerEncodedMysqlFieldCodec(IntegerEncodedField<T> field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(),
				11,
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		Integer integerValue = field.getCodec().encode(field.getValue());
		try{
			if(integerValue == null){
				ps.setNull(parameterIndex, Types.INTEGER);
			}else{
				ps.setLong(parameterIndex, integerValue);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public T fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			int value = rs.getInt(field.getKey().getColumnName());
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
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.INT;
	}

}
