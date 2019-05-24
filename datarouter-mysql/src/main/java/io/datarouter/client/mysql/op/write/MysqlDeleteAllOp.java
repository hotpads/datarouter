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
package io.datarouter.client.mysql.op.write;

import java.sql.Connection;
import java.sql.PreparedStatement;

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.DatarouterMysqlStatement;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.client.mysql.util.SqlBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlDeleteAllOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Integer>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final Config config;

	public MysqlDeleteAllOp(Datarouter datarouter, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Config config){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.fieldInfo = fieldInfo;
		this.config = config;
	}

	@Override
	public Integer runOnce(){
		String sql = SqlBuilder.deleteAll(config, fieldInfo.getTableName());
		Connection connection = getConnection(fieldInfo.getClientId());
		PreparedStatement statement = new DatarouterMysqlStatement().append(sql).toPreparedStatement(connection);
		return MysqlTool.update(statement);
	}

}
