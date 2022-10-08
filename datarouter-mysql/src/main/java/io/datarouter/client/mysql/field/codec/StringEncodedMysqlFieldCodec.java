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
package io.datarouter.client.mysql.field.codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import io.datarouter.client.mysql.ddl.domain.CharSequenceSqlColumn;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.util.CommonFieldSizes;

public class StringEncodedMysqlFieldCodec<T>
extends BaseMysqlFieldCodec<T,StringEncodedField<T>>{

	public static final int DEFAULT_STRING_LENGTH = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;
	public static final MysqlCharacterSet DEFAULT_CHARACTER_SET = MysqlCharacterSet.utf8mb4;
	public static final MysqlCollation DEFAULT_COLLATION = MysqlCollation.utf8mb4_bin;

	private static final int MAX_LENGTH_VARCHAR = 1024;

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable, StringEncodedField<T> field){
		boolean nullable = allowNullable && field.getKey().isNullable();
		return new CharSequenceSqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(field),
				getNormalizedSize(field.getSize()),
				nullable,
				false,
				field.getCodec().encode(field.getKey().getDefaultValue()),
				DEFAULT_CHARACTER_SET,
				DEFAULT_COLLATION);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, StringEncodedField<T> field){
		try{
			if(field.getValue() == null){
				ps.setNull(parameterIndex, Types.VARCHAR);
			}else{
				ps.setString(parameterIndex, field.getCodec().encode(field.getValue()));
			}
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public T fromMysqlResultSetButDoNotSet(ResultSet rs, StringEncodedField<T> field){
		try{
			String string = rs.getString(field.getKey().getColumnName());
			return field.getCodec().decode(string);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(StringEncodedField<T> field){
		return getColumnType(field.getSize());
	}

	public static MysqlColumnType getColumnType(int size){
		if(size <= MAX_LENGTH_VARCHAR){
			return MysqlColumnType.VARCHAR;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_TEXT){
			return MysqlColumnType.TEXT;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT){
			return MysqlColumnType.MEDIUMTEXT;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_LONGTEXT){
			return MysqlColumnType.LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + size);
	}

	public static int getNormalizedSize(int size){
		if(size <= MAX_LENGTH_VARCHAR){
			return size;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_TEXT){
			return CommonFieldSizes.MAX_LENGTH_TEXT;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT){
			return CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT;
		}
		if(size <= CommonFieldSizes.MAX_LENGTH_LONGTEXT){
			return CommonFieldSizes.INT_LENGTH_LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + size);
	}

}
