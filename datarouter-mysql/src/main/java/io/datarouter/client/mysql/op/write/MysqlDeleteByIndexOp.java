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
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;

public class MysqlDeleteByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>>
extends BaseMysqlOp<Long>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Config config;
	private final Collection<IK> entryKeys;

	public MysqlDeleteByIndexOp(Datarouter datarouter, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder, Collection<IK> entryKeys, Config config){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, shouldAutoCommit(entryKeys));
		this.fieldInfo = fieldInfo;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.entryKeys = entryKeys;
		this.config = config;
	}

	@Override
	public Long runOnce(){
		Connection connection = getConnection();
		long numModified = 0;
		for(List<IK> batch : Scanner.of(entryKeys).batch(MysqlReaderNode.DEFAULT_ITERATE_BATCH_SIZE)){
			PreparedStatement statement = mysqlPreparedStatementBuilder.deleteMulti(config, fieldInfo.getTableName(),
					batch, MysqlTableOptions.make(fieldInfo.getSampleFielder())).toPreparedStatement(connection);
			numModified += MysqlTool.update(statement);
		}
		return numModified;
	}

	private static <IK extends PrimaryKey<IK>> boolean shouldAutoCommit(Collection<IK> keys){
		return CollectionTool.size(keys) <= 1;
	}

}
