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
package io.datarouter.client.mysql.op.read;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.tuple.Range;

public class MysqlGetPrimaryKeyRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<PK>>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final Collection<Range<PK>> ranges;
	private final Config config;
	private final MysqlClientType mysqlClientType;

	public MysqlGetPrimaryKeyRangesOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			MysqlFieldCodecFactory fieldCodecFactory,
			MysqlSqlFactory mysqlSqlFactory,
			Collection<Range<PK>> ranges,
			Config config,
			MysqlClientType mysqlClientType){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.ranges = ranges;
		this.config = config;
		this.mysqlClientType = mysqlClientType;
	}

	@Override
	public List<PK> runOnce(){
		String opName = SortedStorageReader.OP_getKeysInRange;
		Connection connection = getConnection();
		String tableName = fieldInfo.getTableName();
		String indexName = fieldInfo.getDisableForcePrimary() ? null : MysqlTool.PRIMARY_KEY_INDEX_NAME;
		PreparedStatement statement = mysqlSqlFactory
				.createSql(getClientId(), tableName)
				.getInRanges(
						tableName,
						config,
						fieldInfo.getPrimaryKeyFields(),
						ranges,
						fieldInfo.getPrimaryKeyFields(),
						indexName)
				.prepare(connection);
		List<PK> result = MysqlTool.selectPrimaryKeys(fieldCodecFactory, fieldInfo, statement);
		DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", fieldInfo.getClientId().getName(),
				fieldInfo.getNodeName(), 1L);
		DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", fieldInfo.getClientId().getName(),
				fieldInfo.getNodeName(), CollectionTool.size(result));
		return result;
	}

}
