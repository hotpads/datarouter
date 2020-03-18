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
package io.datarouter.client.mysql.field.codec.custom;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BasePrimitiveMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;

public class LongDateMysqlFieldCodec
extends BasePrimitiveMysqlFieldCodec<Date,Field<Date>>{

	public LongDateMysqlFieldCodec(){//no-arg for reflection
		this(null);
	}

	public LongDateMysqlFieldCodec(Field<Date> field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(),
				20,
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.BIGINT);
			}else{
				ps.setLong(parameterIndex, field.getValue().getTime());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Date fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			long value = rs.getLong(field.getKey().getColumnName());
			return rs.wasNull() ? null : new Date(value);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.BIGINT;
	}

}
