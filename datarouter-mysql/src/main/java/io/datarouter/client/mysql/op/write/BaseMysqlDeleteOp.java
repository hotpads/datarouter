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

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.CollectionTool;

public abstract class BaseMysqlDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends UniqueKey<?>>
extends BaseMysqlOp<Long>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> databeanFieldInfo;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final MysqlClientType mysqlClientType;
	private final Collection<? extends IK> keys;
	private final Config config;
	private final String indexName;
	private final String opName;

	public BaseMysqlDeleteOp(
			Datarouter datarouter,
			PhysicalDatabeanFieldInfo<PK,D,F> databeanFieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlClientType mysqlClientType,
			Collection<? extends IK> keys,
			Config config,
			String indexName,
			String opName){
		super(datarouter, databeanFieldInfo.getClientId(), Isolation.DEFAULT, shouldAutoCommit(keys));
		this.databeanFieldInfo = databeanFieldInfo;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.mysqlClientType = mysqlClientType;
		this.keys = keys;
		this.config = config;
		this.indexName = indexName;
		this.opName = opName;
	}

	@Override
	public Long runOnce(){
		Connection connection = getConnection();
		String tableName = databeanFieldInfo.getTableName();
		String clientName = databeanFieldInfo.getClientId().getName();
		String nodeName = databeanFieldInfo.getNodeName() + "." + indexName;
		long totalModified = 0;
		for(List<? extends IK> batch : Scanner.of(keys).batch(Config.DEFAULT_INPUT_BATCH_SIZE).iterable()){
			PreparedStatement statement = mysqlSqlFactory
					.createSql(getClientId(), tableName)
					.deleteMulti(tableName, config, batch)
					.prepare(connection);
			int modified = MysqlTool.update(statement);
			totalModified += modified;
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", clientName, nodeName, 1L);
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", clientName, nodeName, modified);
			TracerTool.appendToSpanInfo("deleted " + modified + '/' + batch.size());
		}
		return totalModified;
	}

	private static boolean shouldAutoCommit(Collection<?> keys){
		return CollectionTool.size(keys) <= 1;
	}

}
