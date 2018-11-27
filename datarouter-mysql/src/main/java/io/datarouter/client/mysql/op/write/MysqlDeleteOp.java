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
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.iterable.BatchingIterable;

public class MysqlDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Long>{

	private final PhysicalNode<PK,D,F> node;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Collection<PK> keys;
	private final Config config;

	public MysqlDeleteOp(Datarouter datarouter, PhysicalNode<PK,D,F> node,
			MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder, Collection<PK> keys, Config config){
		super(datarouter, node.getClientNames(), Isolation.DEFAULT, shouldAutoCommit(keys));
		this.node = node;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.keys = keys;
		this.config = Config.nullSafe(config);
	}

	@Override
	public Long runOnce(){
		Connection connection = getConnection(node.getFieldInfo().getClientId().getName());
		long numModified = 0;
		for(List<PK> keyBatch : new BatchingIterable<>(keys, config.getIterateBatchSize())){
			PreparedStatement statement = mysqlPreparedStatementBuilder.deleteMulti(config, node.getFieldInfo()
					.getTableName(), keyBatch, MysqlTableOptions.make(node.getFieldInfo()))
					.toPreparedStatement(connection);
			numModified += MysqlTool.update(statement);
		}
		return numModified;
	}

	private static boolean shouldAutoCommit(Collection<?> keys){
		return CollectionTool.size(keys) <= 1;
	}

}
