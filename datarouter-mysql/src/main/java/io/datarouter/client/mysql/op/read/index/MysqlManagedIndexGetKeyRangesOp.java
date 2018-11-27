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
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
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
import io.datarouter.util.tuple.Range;

public class MysqlManagedIndexGetKeyRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseMysqlOp<List<IK>>{

	private final Collection<Range<IK>> ranges;
	private final PhysicalNode<PK,D,F> node;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Config config;
	private final DatabeanFieldInfo<IK,IE,IF> fieldInfo;

	public MysqlManagedIndexGetKeyRangesOp(Datarouter datarouter, PhysicalNode<PK,D,F> node,
			MysqlFieldCodecFactory fieldCodecFactory, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			DatabeanFieldInfo<IK,IE,IF> fieldInfo, Collection<Range<IK>> ranges, Config config){
		super(datarouter, node.getClientNames(), Isolation.DEFAULT, true);
		this.ranges = ranges;
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.config = config;
		this.fieldInfo = fieldInfo;
	}

	@Override
	public List<IK> runOnce(){
		Client client = node.getClient();
		String tableName = node.getFieldInfo().getTableName();
		String indexName = fieldInfo.getTableName();
		String nodeName = tableName + "." + indexName;
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		Connection connection = getConnection(node.getFieldInfo().getClientId().getName());
		PreparedStatement statement = mysqlPreparedStatementBuilder.getInRanges(config, tableName, fieldInfo
				.getPrimaryKeyFields(), ranges, fieldInfo.getPrimaryKeyFields(), indexName, MysqlTableOptions.make(
				fieldInfo)).toPreparedStatement(connection);
		List<IK> result = MysqlTool.selectIndexEntryKeys(fieldCodecFactory, fieldInfo, statement);
		DatarouterCounters.incClientNodeCustom(client.getType(), opName + " selects", client.getName(), nodeName, 1L);
		DatarouterCounters.incClientNodeCustom(client.getType(), opName + " rows", client.getName(), nodeName, result
				.size());
		return result;
	}

}
