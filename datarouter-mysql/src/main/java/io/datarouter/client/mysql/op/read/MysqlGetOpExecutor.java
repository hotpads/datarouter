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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.iterable.BatchingIterable;

@Singleton
public class MysqlGetOpExecutor{

	@Inject
	private MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,
			T extends Comparable<? super T>>
	List<T> execute(MysqlReaderNode<PK,D,F> node, String opName, Collection<? extends FieldSet<?>> keys, Config config,
			List<Field<?>> selectFields, Function<PreparedStatement,List<T>> select, Connection connection){
		DatabeanFieldInfo<PK,D,F> fieldInfo = node.getFieldInfo();
		List<? extends FieldSet<?>> dedupedSortedKeys = keys.stream()
				.distinct()
				.sorted()
				.collect(Collectors.toList());
		List<T> result = new ArrayList<>(keys.size());
		for(List<? extends FieldSet<?>> keyBatch : new BatchingIterable<>(dedupedSortedKeys, config
				.getIterateBatchSize())){
			PreparedStatement ps = mysqlPreparedStatementBuilder.getMulti(config, fieldInfo.getTableName(),
					selectFields, keyBatch, MysqlTableOptions.make(fieldInfo)).toPreparedStatement(connection);
			DatarouterCounters.incClientNodeCustom(node.getClient().getType(), opName + " selects", fieldInfo
					.getClientId().getName(), node.getName(), 1L);

			List<T> resultBatch = select.apply(ps);

			DatarouterCounters.incClientNodeCustom(node.getClient().getType(), opName + " rows", fieldInfo
					.getClientId().getName(), node.getName(), result.size());
			result.addAll(resultBatch);
		}
		TracerTool.appendToSpanInfo(TracerThreadLocal.get(), "[got " + result.size() + "/" + keys.size() + "]");
		return result;
	}

}
