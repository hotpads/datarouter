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
package io.datarouter.client.mysql.op.write;

import java.util.Collection;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.MysqlUniqueIndexTool;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlUniqueIndexDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlDeleteOp<PK,D,F,UniqueKey<PK>>{

	public MysqlUniqueIndexDeleteOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<PK,D,F> databeanFieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlClientType mysqlClientType,
			Collection<? extends UniqueKey<PK>> keys,
			Config config,
			String opName){
		super(datarouter,
				databeanFieldInfo,
				mysqlSqlFactory,
				mysqlClientType,
				keys,
				config,
				MysqlUniqueIndexTool.searchIndex(databeanFieldInfo.getUniqueIndexes(), keys),
				opName);
	}

}
