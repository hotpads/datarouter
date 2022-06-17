/*
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
package io.datarouter.gcp.spanner.op.read.index;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.ListTool;

public class SpannerLookupUniqueOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseReadIndexOp<PK,D>{

	private final Collection<? extends UniqueKey<PK>> keys;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public SpannerLookupUniqueOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<? extends UniqueKey<PK>> keys,
			Config config,
			SpannerFieldCodecs fieldCodecs){
		super(client, config, fieldCodecs, fieldInfo.getTableName());
		this.keys = keys;
		this.fieldInfo = fieldInfo;
	}

	@Override
	public List<D> wrappedCall(){
		if(keys == null || keys.isEmpty()){
			return List.of();
		}
		String indexName = getIndexName(keys.iterator().next());
		ReadOption[] readOptions = config.findLimit()
				.map(limit -> new ReadOption[]{Options.limit(limit)})
				.orElseGet(() -> new ReadOption[]{});
		try(ReadOnlyTransaction txn = client.readOnlyTransaction()){
			try(ResultSet rs = txn.readUsingIndex(
					tableName,
					indexName,
					buildKeySet(),
					fieldInfo.getPrimaryKeyFieldColumnNames(),
					readOptions)){
				List<PK> keyList = createFromResultSet(
						rs,
						fieldInfo.getPrimaryKeySupplier(),
						fieldInfo.getPrimaryKeyFields());
				try(ResultSet databeanRs = txn.read(
						tableName,
						buildKeySet(keyList),
						fieldInfo.getFieldColumnNames(),
						readOptions)){
					return createFromResultSet(
							databeanRs,
							fieldInfo.getDatabeanSupplier(),
							fieldInfo.getFields());
				}
			}
		}
	}

	@Override
	public KeySet buildKeySet(){
		KeySet.Builder keySetBuilder = KeySet.newBuilder();
		keys.stream()
				.map(this::primaryKeyConversion)
				.forEach(keySetBuilder::addKey);
		return keySetBuilder.build();
	}

	private <UK extends UniqueKey<PK>> String getIndexName(UK key){
		List<String> indexFields = key.getFieldNames();
		for(Entry<String,List<Field<?>>> uniqueIndex : fieldInfo.getUniqueIndexes().entrySet()){
			List<String> fieldNames = Scanner.of(uniqueIndex.getValue())
					.map(field -> field.getKey().getName())
					.list();
			if(ListTool.compare(indexFields, fieldNames) == 0){
				return uniqueIndex.getKey();
			}
		}
		throw new RuntimeException("Cannot find index Name for key");
	}

	private Key primaryKeyConversion(UniqueKey<PK> key){
		Key.Builder mutationKey = Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : fieldCodecs.createCodecs(key.getFields())){
			mutationKey = codec.setKey(mutationKey);
		}
		return mutationKey.build();
	}

}
