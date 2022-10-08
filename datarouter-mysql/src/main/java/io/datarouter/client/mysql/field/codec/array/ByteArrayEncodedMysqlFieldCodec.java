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
package io.datarouter.client.mysql.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayEncodedMysqlFieldCodec<T>
extends BaseMysqlFieldCodec<T,ByteArrayEncodedField<T>>{

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, ByteArrayEncodedField<T> field){
		boolean nullable = allowNullable && field.getKey().isNullable();
		MysqlColumnType type = getMysqlColumnType(field);
		int size = type == MysqlColumnType.LONGBLOB ? Integer.MAX_VALUE : field.getKey().getSize();
		return new SqlColumn(field.getKey().getColumnName(), type, size, nullable, false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, ByteArrayEncodedField<T> field){
		try{
			ps.setBytes(parameterIndex, field.getCodec().encode(field.getValue()));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public T fromMysqlResultSetButDoNotSet(ResultSet rs, ByteArrayEncodedField<T> field){
		try{
			byte[] bytes = rs.getBytes(field.getKey().getColumnName());
			return field.getCodec().decode(bytes);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(ByteArrayEncodedField<T> field){
		int size = field.getKey().getSize();
		return getMysqlColumnType(size);
	}

	public static MysqlColumnType getMysqlColumnType(int size){
		if(size <= CommonFieldSizes.MAX_LENGTH_VARBINARY){
			return MysqlColumnType.VARBINARY;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_LONGBLOB){
			return MysqlColumnType.LONGBLOB;
		}
		throw new IllegalArgumentException("Size:" + size + " is larger than max supported size: "
				+ CommonFieldSizes.MAX_LENGTH_LONGBLOB + " (CommonFieldSizes.MAX_LENGTH_LONGBLOB)");
	}

}
