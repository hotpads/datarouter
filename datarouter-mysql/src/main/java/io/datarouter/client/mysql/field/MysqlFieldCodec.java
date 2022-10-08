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

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.model.field.Field;

public interface MysqlFieldCodec<T,F extends Field<T>>{

	MysqlColumnType getMysqlColumnType(F field);
	SqlColumn getSqlColumnDefinition(boolean allowNullable, F field);
	String getIntroducedParameter(MysqlLiveTableOptions mysqlTableOptions, boolean disableIntroducer, F field);
	String getSqlParameter();
	void setPreparedStatementValue(PreparedStatement ps, int parameterIndex, F field);
	T fromMysqlResultSetButDoNotSet(ResultSet rs, F field);

}
