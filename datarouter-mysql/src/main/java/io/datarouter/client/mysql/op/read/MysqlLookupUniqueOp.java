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

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;

public class MysqlLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<D>>{
	private static final Logger logger = LoggerFactory.getLogger(MysqlLookupUniqueOp.class);

	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlGetOpExecutor mysqlGetOpExecutor;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final String opName;
	private final Collection<? extends UniqueKey<PK>> keys;
	private final Config config;
	private final String indexName;

	public MysqlLookupUniqueOp(Datarouter datarouter, MysqlFieldCodecFactory fieldCodecFactory,
			MysqlGetOpExecutor mysqlGetOpExecutor, PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, String opName,
			Collection<? extends UniqueKey<PK>> keys, Config config){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
		this.mysqlGetOpExecutor = mysqlGetOpExecutor;
		this.indexName = searchIndex(fieldInfo.getUniqueIndexes(), keys);
	}

	private static <PK extends PrimaryKey<PK>> String searchIndex(Map<String,List<Field<?>>> uniqueIndexes,
			Collection<? extends UniqueKey<PK>> keys){
		Optional<? extends UniqueKey<PK>> findFirst = CollectionTool.findFirst(keys);
		if(findFirst.isEmpty()){
			logger.warn("no keys to guess the index", new Exception());
			return null;
		}
		List<?> dataFieldKeys = findFirst.get().getFields().stream()
				.map(Field::getKey)
				.collect(Collectors.toList());
		for(Entry<String,List<Field<?>>> uniqueIndex : uniqueIndexes.entrySet()){
			List<?> defFieldKeys = uniqueIndex.getValue().stream()
					.map(Field::getKey)
					.collect(Collectors.toList());
			if(defFieldKeys.equals(dataFieldKeys)){
				return uniqueIndex.getKey();
			}
		}
		logger.warn("matching index not found uniqueIndexes={} findFirst={}", uniqueIndexes, findFirst,
				new Exception());
		return null;
	}

	@Override
	public List<D> runOnce(){
		return mysqlGetOpExecutor.execute(fieldInfo, opName, keys, config, fieldInfo.getFields(), this::select,
				getConnection(), indexName);
	}

	private List<D> select(PreparedStatement ps){
		return MysqlTool.selectDatabeans(fieldCodecFactory, fieldInfo.getDatabeanSupplier(), fieldInfo.getFields(), ps);
	}

}
