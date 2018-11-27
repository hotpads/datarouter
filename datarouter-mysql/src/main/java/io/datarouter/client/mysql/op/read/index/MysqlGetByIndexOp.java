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
package io.datarouter.client.mysql.op.read.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.iterable.BatchingIterable;

public class MysqlGetByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseMysqlOp<List<D>>{

	private final PhysicalNode<PK,D,F> node;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Collection<IK> entryKeys;
	private final Config config;
	private final DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo;

	public MysqlGetByIndexOp(Datarouter datarouter, PhysicalNode<PK,D,F> node, MysqlFieldCodecFactory fieldCodecFactory,
			MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<IK> entryKeys, Config config){
		super(datarouter, node.getClientNames(), Isolation.DEFAULT, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.entryKeys = entryKeys;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		Connection connection = getConnection(node.getFieldInfo().getClientId().getName());
		List<D> result = new ArrayList<>();
		for(List<IK> batch : new BatchingIterable<>(entryKeys, MysqlReaderNode.DEFAULT_ITERATE_BATCH_SIZE)){
			Client client = node.getClient();
			String opName = IndexedStorageReader.OP_getByIndex;
			String tableName = node.getFieldInfo().getTableName();
			String indexName = indexEntryFieldInfo.getTableName();
			String nodeName = tableName + "." + indexName;
			DatarouterCounters.incClientNodeCustom(client.getType(), opName + " selects", client.getName(), nodeName,
					1L);
			PreparedStatement statement = mysqlPreparedStatementBuilder.getWithPrefixes(config, tableName, indexName,
					node.getFieldInfo().getFields(), batch, null, MysqlTableOptions.make(node.getFieldInfo()))
					.toPreparedStatement(connection);
			List<D> batchResult = MysqlTool.selectDatabeans(fieldCodecFactory, node.getFieldInfo(), statement);
			DatarouterCounters.incClientNodeCustom(client.getType(), opName + " rows", client.getName(), nodeName,
					result.size());
			result.addAll(batchResult);
		}
		return result;
	}

}
