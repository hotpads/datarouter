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
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlDeleteByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>>
extends BaseMysqlDeleteOp<PK,D,F,IK>{

	public MysqlDeleteByIndexOp(
			DatarouterClients datarouterClients,
			PhysicalDatabeanFieldInfo<PK,D,F> databeanfieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlClientType mysqlClientType,
			Collection<IK> keys,
			Config config,
			String indexName,
			String opName){
		super(datarouterClients, databeanfieldInfo, mysqlSqlFactory, mysqlClientType, keys, config, indexName, opName);
	}

}
