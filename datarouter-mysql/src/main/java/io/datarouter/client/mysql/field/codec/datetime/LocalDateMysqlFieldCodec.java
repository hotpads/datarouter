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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.LocalDateField;

public class LocalDateMysqlFieldCodec extends BaseMysqlFieldCodec<LocalDate,LocalDateField>{

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, LocalDateField field){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(field),
				null,
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, LocalDateField field){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DATE);
			}else{
				ps.setDate(parameterIndex, Date.valueOf(field.getValue()), java.util.Calendar.getInstance());
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public LocalDate fromMysqlResultSetButDoNotSet(ResultSet rs, LocalDateField field){
		try{
			Date date = rs.getDate(field.getKey().getColumnName(), java.util.Calendar.getInstance());
			if(rs.wasNull()){
				return null;
			}
			return date.toLocalDate();
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(LocalDateField field){
		return MysqlColumnType.DATE;
	}

}
