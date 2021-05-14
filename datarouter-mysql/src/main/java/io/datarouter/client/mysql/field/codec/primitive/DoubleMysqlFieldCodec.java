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
package io.datarouter.client.mysql.field.codec.primitive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BasePrimitiveMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.DoubleField;

public class DoubleMysqlFieldCodec
extends BasePrimitiveMysqlFieldCodec<Double,Field<Double>>{

	public DoubleMysqlFieldCodec(){//no-arg for reflection
		this((DoubleField)null);
	}

	public DoubleMysqlFieldCodec(DoubleField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		String defaultValue = null;
		if(field.getKey().getDefaultValue() != null){
			defaultValue = field.getKey().getDefaultValue().toString();
		}
		return new SqlColumn(field.getKey().getColumnName(), getMysqlColumnType(), null, nullable, false, defaultValue);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.DOUBLE);
			}else{
				ps.setDouble(parameterIndex, field.getValue());
			}
		}catch(SQLException e){
			throw new DataAccessException("error building query sql=" + ps, e);
		}
	}

	@Override
	public Double fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			double value = rs.getDouble(field.getKey().getColumnName());
			return rs.wasNull() ? null : value;
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.DOUBLE;
	}

}
