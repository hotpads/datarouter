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
package io.datarouter.client.mysql.field.codec.base;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.model.field.Field;

public abstract class BaseMysqlFieldCodec<T,F extends Field<T>> implements MysqlFieldCodec<T,F>{

	@Override
	public String getSqlParameter(){
		return "?";
	}

	@Override
	public String getIntroducedParameter(MysqlLiveTableOptions mysqlTableOptions, boolean disableIntroducer, F field){
		return introduce(mysqlTableOptions, "?", disableIntroducer, field);
	}

	private String introduce(MysqlLiveTableOptions mysqlTableOptions, String parameter, boolean disableIntroducer,
			F field){
		if(!shouldIntroduceLiteral(mysqlTableOptions, disableIntroducer, field)){
			return parameter;
		}
		return "_" + mysqlTableOptions.getCharacterSet().name() + " " + parameter + " COLLATE "
				+ mysqlTableOptions.getCollation().name();
	}

	private boolean shouldIntroduceLiteral(MysqlLiveTableOptions mysqlTableOptions, boolean disableIntroducer, F field){
		if(!getMysqlColumnType(field).isIntroducible() || disableIntroducer){
			return false;
		}
		boolean characterSetConnectionMismatch = mysqlTableOptions
				.getCharacterSet() != MysqlConnectionPoolHolder.CHARACTER_SET_CONNECTION;
		boolean collationConnectionMismatch = mysqlTableOptions
				.getCollation() != MysqlConnectionPoolHolder.COLLATION_CONNECTION;
		// literals only benefit from introducer if the column's settings differ from the connection's settings
		return characterSetConnectionMismatch || collationConnectionMismatch;
	}

}
