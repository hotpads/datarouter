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
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.util.CommonFieldSizes;

public class StringMysqlFieldCodec
extends BaseMysqlFieldCodec<String,StringField>{

	private static final int MAX_LENGTH_VARCHAR = 1024;

	public static final int DEFAULT_STRING_LENGTH = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;
	public static final MysqlCharacterSet DEFAULT_CHARACTER_SET = MysqlCharacterSet.utf8mb4;
	public static final MysqlCollation DEFAULT_COLLATION = MysqlCollation.utf8mb4_bin;

	public StringMysqlFieldCodec(StringField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		boolean nullable = allowNullable && field.getKey().isNullable();
		return new CharSequenceSqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(),
				getNormalizedSize(),
				nullable,
				false,
				field.getKey().getDefaultValue(),
				DEFAULT_CHARACTER_SET,
				DEFAULT_COLLATION);
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

	private int getNormalizedSize(){
		if(field.getSize() <= MAX_LENGTH_VARCHAR){
			return field.getSize();
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_TEXT){
			return CommonFieldSizes.MAX_LENGTH_TEXT;
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT){
			return CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT;
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_LONGTEXT){
			return CommonFieldSizes.INT_LENGTH_LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		if(field.getSize() <= MAX_LENGTH_VARCHAR){
			return MysqlColumnType.VARCHAR;
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_TEXT){
			return MysqlColumnType.TEXT;
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_MEDIUMTEXT){
			return MysqlColumnType.MEDIUMTEXT;
		}
		if(field.getSize() <= CommonFieldSizes.MAX_LENGTH_LONGTEXT){
			return MysqlColumnType.LONGTEXT;
		}
		throw new IllegalArgumentException("Unknown size:" + field.getSize());
	}

}
