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
package io.datarouter.client.mysql.field.codec.array;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseMysqlFieldCodec;
import io.datarouter.model.field.imp.array.PrimitiveLongArrayField;
import io.datarouter.util.exception.NotImplementedException;

public class PrimitiveLongArrayMysqlFieldCodec
extends BaseMysqlFieldCodec<long[],PrimitiveLongArrayField>{

	public PrimitiveLongArrayMysqlFieldCodec(){//no-arg for reflection
		this(null);
	}

	public PrimitiveLongArrayMysqlFieldCodec(PrimitiveLongArrayField field){
		super(field);
	}


	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		throw new NotImplementedException();
	}

	@Override
	public long[] fromMysqlResultSetButDoNotSet(ResultSet rs){
		throw new NotImplementedException();
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		throw new NotImplementedException();
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		throw new NotImplementedException();
	}

}
