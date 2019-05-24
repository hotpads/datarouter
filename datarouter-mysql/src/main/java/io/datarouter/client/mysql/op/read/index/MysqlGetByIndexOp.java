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

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
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

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Collection<IK> indexKeys;
	private final Config config;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final MysqlClientType mysqlClientType;

	public MysqlGetByIndexOp(Datarouter datarouter, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			MysqlFieldCodecFactory fieldCodecFactory, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			MysqlClientType mysqlClientType, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<IK> indexKeys, Config config){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.mysqlClientType = mysqlClientType;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.indexKeys = indexKeys;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		Connection connection = getConnection(fieldInfo.getClientId());
		String opName = IndexedStorageReader.OP_getByIndex;
		String tableName = fieldInfo.getTableName();
		String indexName = indexEntryFieldInfo.getIndexName();
		String nodeName = tableName + "." + indexName;
		List<D> result = new ArrayList<>();
		for(List<IK> batch : new BatchingIterable<>(indexKeys, MysqlReaderNode.DEFAULT_ITERATE_BATCH_SIZE)){
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", fieldInfo.getClientId()
					.getName(), nodeName, 1L);
			PreparedStatement statement = mysqlPreparedStatementBuilder.getWithPrefixes(config, tableName, indexName,
					fieldInfo.getFields(), batch, null, MysqlTableOptions.make(fieldInfo.getSampleFielder()))
					.toPreparedStatement(connection);
			List<D> batchResult = MysqlTool.selectDatabeans(fieldCodecFactory, fieldInfo.getDatabeanSupplier(),
					fieldInfo.getFields(), statement);
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", fieldInfo.getClientId().getName(),
					nodeName, result.size());
			TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "[got " + result.size() + "/" + batch.size() + "]");
			result.addAll(batchResult);
		}
		return result;
	}

}
