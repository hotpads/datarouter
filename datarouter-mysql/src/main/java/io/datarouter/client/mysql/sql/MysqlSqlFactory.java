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
package io.datarouter.client.mysql.sql;

import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptionsRefresher;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.storage.client.ClientId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MysqlSqlFactory{

	@Inject
	private MysqlFieldCodecFactory codecFactory;
	@Inject
	private MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher;

	public MysqlSql createSql(ClientId clientId, String tableName, boolean disableIntroducer){
		var mysqlLiveTableOptions = mysqlLiveTableOptionsRefresher.get(clientId, tableName);
		return new MysqlSql(codecFactory, mysqlLiveTableOptions, disableIntroducer);
	}

	public MysqlSql createSql(MysqlLiveTableOptions mysqlLiveTableOptions, boolean disableIntroducer){
		return new MysqlSql(codecFactory, mysqlLiveTableOptions, disableIntroducer);
	}

}
