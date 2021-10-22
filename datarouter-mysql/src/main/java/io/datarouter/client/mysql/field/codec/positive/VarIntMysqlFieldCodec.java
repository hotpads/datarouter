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
package io.datarouter.client.mysql.field.codec.positive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BasePrimitiveMysqlFieldCodec;
import io.datarouter.model.field.imp.positive.VarIntField;

public class VarIntMysqlFieldCodec
extends BasePrimitiveMysqlFieldCodec<Integer,VarIntField>{

	private final UInt31MysqlFieldCodec uint31MysqlFieldCodec;

	public VarIntMysqlFieldCodec(VarIntField field){
		super(field);
		this.uint31MysqlFieldCodec = new UInt31MysqlFieldCodec(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return uint31MysqlFieldCodec.getSqlColumnDefinition(allowNullable);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		uint31MysqlFieldCodec.setPreparedStatementValue(ps, parameterIndex);
	}

	@Override
	public Integer fromMysqlResultSetButDoNotSet(ResultSet rs){
		Integer value = uint31MysqlFieldCodec.fromMysqlResultSetButDoNotSet(rs);
		return VarIntField.assertInRange(value);
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return uint31MysqlFieldCodec.getMysqlColumnType();
	}

}
