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
package io.datarouter.gcp.spanner.op.read.index.write;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.cloud.spanner.TransactionRunner.TransactionCallable;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.Key;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public abstract class SpannerBaseIndexDelete<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		K extends Key<?>>
extends SpannerBaseOp<Void>{

	protected final DatabaseClient client;
	protected final Config config;
	protected final SpannerFieldCodecRegistry codecRegistry;
	protected final String tableName;
	private final Collection<K> keys;
	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public SpannerBaseIndexDelete(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<K> keys,
			Config config,
			SpannerFieldCodecRegistry codecRegistry){
		super("Spanner delete");
		this.keys = keys;
		this.fieldInfo = fieldInfo;
		this.client = client;
		this.config = config;
		this.codecRegistry = codecRegistry;
		this.tableName = fieldInfo.getTableName();
	}

	@Override
	public Void wrappedCall(){
		if(keys == null || keys.isEmpty()){
			return null;
		}
		String indexName = getIndexName(keys.iterator().next());
		TransactionRunner runner = client.readWriteTransaction();
		TransactionCallable<Void> txn = new TransactionCallable<>(){

			@Nullable
			@Override
			public Void run(TransactionContext transactionContext){
				ResultSet rs;
				if(config.getLimit() != null){
					rs = transactionContext.readUsingIndex(
							tableName,
							indexName,
							buildKeySet(),
							fieldInfo.getPrimaryKeyFieldColumnNames(),
							Options.limit(config.getLimit()));
				}else{
					rs = transactionContext.readUsingIndex(
							tableName,
							indexName,
							buildKeySet(),
							fieldInfo.getPrimaryKeyFieldColumnNames());
				}
				List<PK> keyList = createFromResultSet(
						rs,
						fieldInfo.getPrimaryKeySupplier(),
						fieldInfo.getPrimaryKeyFields());
				Scanner.of(keyList)
						.map(key -> keyToDeleteMutation(key))
						.flush(transactionContext::buffer);
				return null;
			}
		};
		runner.run(txn);
		return null;
	}

	public KeySet buildKeySet(){
		KeySet.Builder keySetBuilder = KeySet.newBuilder();
		keys.stream()
				.map(this::primaryKeyConversion)
				.forEach(keySetBuilder::addKey);
		return keySetBuilder.build();
	}

	protected abstract String getIndexName(K key);

	private com.google.cloud.spanner.Key primaryKeyConversion(K key){
		Builder mutationKey = com.google.cloud.spanner.Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(key.getFields())){
			mutationKey = codec.setKey(mutationKey);
		}
		return mutationKey.build();
	}

	protected Mutation keyToDeleteMutation(PK key){
		Builder mutationKey = com.google.cloud.spanner.Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(key.getFields())){
			mutationKey = codec.setKey(mutationKey);
		}
		return Mutation.delete(tableName, mutationKey.build());
	}

	protected List<PK> createFromResultSet(ResultSet set, Supplier<PK> emtpyObject, List<Field<?>> fields){
		List<? extends SpannerBaseFieldCodec<?,?>> codecs = codecRegistry.createCodecs(fields);
		List<PK> objects = new ArrayList<>();
		while(set.next()){
			PK object = emtpyObject.get();
			codecs.forEach(codec -> codec.setField(object, set));
			objects.add(object);
		}
		return objects;
	}

}
