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
import java.util.Date;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.DateField;

@SuppressWarnings("deprecation")
public class DateMysqlFieldCodec extends BaseMysqlFieldCodec<Date,DateField>{

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, DateField field){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(field),
				field.getNumDecimalSeconds(),
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, DateField field){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				// sql timestamp is MySQL's datetime
				ps.setTimestamp(parameterIndex, new Timestamp(field.getValue().getTime()));
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public Date fromMysqlResultSetButDoNotSet(ResultSet rs, DateField field){
		try{
			Timestamp timeStamp = rs.getTimestamp(field.getKey().getColumnName());
			if(rs.wasNull()){
				return null;
			}
			return new Date(timeStamp.getTime());
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(DateField field){
		return MysqlColumnType.DATETIME;
	}

	public static String getSqlDateString(Date date){
		return new Timestamp(date.getTime()).toString();
	}

}
