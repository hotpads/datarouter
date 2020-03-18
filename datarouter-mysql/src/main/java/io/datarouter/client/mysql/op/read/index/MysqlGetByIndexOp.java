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
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;

public class MysqlGetByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseMysqlOp<List<D>>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> databeanFieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final Collection<IK> indexKeys;
	private final Config config;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final MysqlClientType mysqlClientType;

	public MysqlGetByIndexOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<PK,D,F> databeanFieldInfo,
			MysqlFieldCodecFactory fieldCodecFactory,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlClientType mysqlClientType,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<IK> indexKeys,
			Config config){
		super(datarouter, databeanFieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.databeanFieldInfo = databeanFieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.mysqlClientType = mysqlClientType;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.indexKeys = indexKeys;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		Connection connection = getConnection();
		String tableName = databeanFieldInfo.getTableName();
		String indexName = indexEntryFieldInfo.getIndexName();
		String clientName = databeanFieldInfo.getClientId().getName();
		String nodeName = databeanFieldInfo.getNodeName() + "." + indexName;
		String opName = IndexedStorageReader.OP_getByIndex;
		List<D> result = new ArrayList<>();
		for(List<IK> batch : Scanner.of(indexKeys).batch(MysqlReaderNode.DEFAULT_ITERATE_BATCH_SIZE).iterable()){
			PreparedStatement statement = mysqlSqlFactory
					.createSql(getClientId(), tableName)
					.getWithPrefixes(
							tableName,
							config,
							indexName,
							databeanFieldInfo.getFields(),
							batch,
							null)
					.prepare(connection);
			List<D> batchResult = MysqlTool.selectDatabeans(
					fieldCodecFactory,
					databeanFieldInfo.getDatabeanSupplier(),
					databeanFieldInfo.getFields(),
					statement);
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", clientName, nodeName, 1L);
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", clientName, nodeName, batchResult
					.size());
			TracerTool.appendToSpanInfo("got " + result.size() + '/' + batch.size());
			result.addAll(batchResult);
		}
		return result;
	}

}
