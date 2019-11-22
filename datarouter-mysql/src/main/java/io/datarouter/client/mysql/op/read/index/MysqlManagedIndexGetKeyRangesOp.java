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

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptionsRefresher;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
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
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher;
	private final Config config;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final MysqlClientType mysqlClientType;

	public MysqlManagedIndexGetKeyRangesOp(Datarouter datarouter, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			MysqlFieldCodecFactory fieldCodecFactory, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges, Config config,
			MysqlClientType mysqlClientType){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.ranges = ranges;
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.mysqlLiveTableOptionsRefresher = mysqlLiveTableOptionsRefresher;
		this.config = config;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.mysqlClientType = mysqlClientType;
	}

	@Override
	public List<IK> runOnce(){
		String tableName = fieldInfo.getTableName();
		String indexName = indexEntryFieldInfo.getIndexName();
		String nodeName = tableName + "." + indexName;
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		Connection connection = getConnection();
		MysqlLiveTableOptions mysqlLiveTableOptions = mysqlLiveTableOptionsRefresher.get(getClientId(), tableName);
		PreparedStatement statement = mysqlPreparedStatementBuilder.getInRanges(config, tableName, indexEntryFieldInfo
				.getPrimaryKeyFields(), ranges, indexEntryFieldInfo.getPrimaryKeyFields(), indexName,
				mysqlLiveTableOptions)
				.toPreparedStatement(connection);
		List<IK> result = MysqlTool.selectIndexEntryKeys(fieldCodecFactory, indexEntryFieldInfo, statement);
		DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", fieldInfo.getClientId().getName(),
				nodeName, 1L);
		DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", fieldInfo.getClientId().getName(),
				nodeName, result.size());
		TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
				.ranges(ranges.size())
				.add("indexKeys", result.size()));
		return result;
	}

}
