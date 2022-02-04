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
package io.datarouter.client.mysql.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.StringMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.enums.StringEnum;
import io.datarouter.model.field.imp.enums.StringEnumField;

public class StringEnumMysqlFieldCodec<E extends StringEnum<E>>
extends BaseMysqlFieldCodec<E,StringEnumField<E>>{

	private final StringMysqlFieldCodec stringMysqlFieldCodec;

	public StringEnumMysqlFieldCodec(StringEnumField<E> field){
		super(field);
		stringMysqlFieldCodec = new StringMysqlFieldCodec(StringEnumField.toStringField(field));
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return stringMysqlFieldCodec.getSqlColumnDefinition(allowNullable);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		stringMysqlFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public E fromMysqlResultSetButDoNotSet(ResultSet rs){
		String string = stringMysqlFieldCodec.fromMysqlResultSetButDoNotSet(rs);
		return StringEnum.fromPersistentStringSafe(field.getSampleValue(), string);
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return stringMysqlFieldCodec.getMysqlColumnType();
	}

}
