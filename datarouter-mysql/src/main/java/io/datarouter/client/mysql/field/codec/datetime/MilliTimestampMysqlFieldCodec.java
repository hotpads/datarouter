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
package io.datarouter.client.mysql.field.codec.datetime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.comparable.MilliTimestampEncodedField;
import io.datarouter.model.field.imp.comparable.MilliTimestampEncodedFieldKey;
import io.datarouter.types.MilliTime;

public class MilliTimestampMysqlFieldCodec<T> extends BaseMysqlFieldCodec<T,MilliTimestampEncodedField<T>>{
	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, MilliTimestampEncodedField<T> field){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(field),
				MilliTimestampEncodedFieldKey.NUM_DECIMAL_SECONDS,
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex,
			MilliTimestampEncodedField<T> field){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				MilliTime milliTime = field.getCodec().encode(field.getValue());
				long epochMilli = milliTime.toEpochMilli();
				Timestamp timestamp = new Timestamp(epochMilli);
				ps.setTimestamp(parameterIndex, timestamp);
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public T fromMysqlResultSetButDoNotSet(ResultSet rs, MilliTimestampEncodedField<T> field){
		try{
			Timestamp timestamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return field.getCodec().decode(null);
			}
			long epochMilli = timestamp.getTime();
			MilliTime milliTime = MilliTime.ofEpochMilli(epochMilli);
			return field.getCodec().decode(milliTime);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(MilliTimestampEncodedField<T> field){
		return MysqlColumnType.DATETIME;
	}

}
