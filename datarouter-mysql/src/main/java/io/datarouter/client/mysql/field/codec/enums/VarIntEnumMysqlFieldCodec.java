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
package io.datarouter.client.mysql.field.codec.enums;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.field.imp.enums.VarIntEnumField;
import io.datarouter.util.enums.IntegerEnum;

public class VarIntEnumMysqlFieldCodec<E extends IntegerEnum<E>>
extends BaseMysqlFieldCodec<E,VarIntEnumField<E>>{

	private IntegerEnumMysqlFieldCodec<E> integerEnumMysqlFieldCodec;

	public VarIntEnumMysqlFieldCodec(){
		this(null);
	}

	public VarIntEnumMysqlFieldCodec(VarIntEnumField<E> field){
		super(field);
		this.integerEnumMysqlFieldCodec = new IntegerEnumMysqlFieldCodec<>(VarIntEnumField.toIntegerEnumField(field));
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return integerEnumMysqlFieldCodec.getSqlColumnDefinition(allowNullable);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		integerEnumMysqlFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public E fromMysqlResultSetButDoNotSet(ResultSet rs){
		return integerEnumMysqlFieldCodec.fromMysqlResultSetButDoNotSet(rs);
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return integerEnumMysqlFieldCodec.getMysqlColumnType();
	}

}
