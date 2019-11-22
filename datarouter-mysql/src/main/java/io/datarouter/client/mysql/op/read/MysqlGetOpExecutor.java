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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptions;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptionsRefresher;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;

@Singleton
public class MysqlGetOpExecutor{

	private static final int BATCH_SIZE = 100;

	@Inject
	private MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	@Inject
	private MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher;
	@Inject
	private MysqlClientType mysqlClientType;

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,
			T extends Comparable<? super T>>
	List<T> execute(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, String opName, Collection<? extends FieldSet<?>> keys,
			Config config, List<Field<?>> selectFields, Function<PreparedStatement,List<T>> select,
			Connection connection, String indexName){
		Stream<? extends FieldSet<?>> dedupedSortedKeys = keys.stream()
				.distinct()
				.sorted();
		String tableName = fieldInfo.getTableName();
		List<T> result = new ArrayList<>(keys.size());
		for(List<? extends FieldSet<?>> keyBatch : Scanner.of(dedupedSortedKeys).batch(config.optInputBatchSize()
				.orElse(BATCH_SIZE)).iterable()){
			MysqlLiveTableOptions mysqlLiveTableOptions = mysqlLiveTableOptionsRefresher.get(fieldInfo.getClientId(),
					tableName);
			PreparedStatement ps = mysqlPreparedStatementBuilder.getMulti(config, tableName, selectFields, keyBatch,
					indexName, mysqlLiveTableOptions)
					.toPreparedStatement(connection);
			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " selects", fieldInfo.getClientId()
					.getName(), fieldInfo.getNodeName(), 1L);

			List<T> resultBatch = select.apply(ps);

			DatarouterCounters.incClientNodeCustom(mysqlClientType, opName + " rows", fieldInfo.getClientId().getName(),
					fieldInfo.getNodeName(), result.size());
			result.addAll(resultBatch);
		}
		return result;
	}

}
