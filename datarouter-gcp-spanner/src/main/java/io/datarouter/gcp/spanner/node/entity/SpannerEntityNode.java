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
package io.datarouter.gcp.spanner.node.entity;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.KeyRange;
import com.google.cloud.spanner.KeyRange.Endpoint;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.read.SpannerBaseReadOp;
import io.datarouter.gcp.spanner.op.write.SpannerBaseWriteOp;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.entity.BasePhysicalEntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;

public class SpannerEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BasePhysicalEntityNode<EK,E>{

	private static final int DEFAULT_GET_KEYS_LIMIT = 1000;

	private final SpannerClientManager clientManager;
	private final SpannerFieldCodecRegistry codecRegistry;
	private final ClientId clientId;

	public SpannerEntityNode(
			EntityNodeParams<EK,E> entityNodeParams,
			ClientTableNodeNames clientTableNodeNames,
			SpannerClientManager clientManager,
			SpannerFieldCodecRegistry fieldCodecRegistry,
			ClientId clientId){
		super(entityNodeParams, clientTableNodeNames);
		this.clientManager = clientManager;
		this.codecRegistry = fieldCodecRegistry;
		this.clientId = clientId;
	}

	@Override
	public List<E> getEntities(Collection<EK> entityKeys, Config config){
		throw new UnsupportedOperationException("Spanner doesn't support getEntities");
	}

	@Override
	public void deleteMultiEntities(Collection<EK> entityKeys, Config config){
		var op = new EntityDeleteOp(
				entityKeys,
				clientManager.getDatabaseClient(clientId),
				entityFieldInfo.getEntityTableName(),
				config);
		op.wrappedCall();
	}

	@Override
	public List<EK> listEntityKeys(EK startKey, boolean startKeyInclusive, Config config){
		Integer limit = config.optLimit().orElse(DEFAULT_GET_KEYS_LIMIT);
		config.setLimit(limit);
		return entityFieldInfo.getEntityPartitioner().scanAllPartitions()
				.map(partition -> new EntityListOp(clientManager.getDatabaseClient(clientId), config, codecRegistry,
						entityFieldInfo.getEntityTableName(), partition, startKey, startKeyInclusive))
				.map(EntityListOp::wrappedCall)
				.concatenate(Scanner::of)
				.sorted()
				.limit(limit)
				.list();
	}

	private class EntityDeleteOp extends SpannerBaseWriteOp<EK>{

		public EntityDeleteOp(Collection<EK> entityKeys, DatabaseClient client, String tableName, Config config){
			super(client, tableName, config, entityKeys);
		}

		@Override
		public Collection<Mutation> getMutations(){
			return Scanner.of(values).map(this::keyToDeleteMutation).list();
		}

		private Mutation keyToDeleteMutation(EK key){
			Builder keyBuilder = Key.newBuilder();
			keyBuilder.append(entityFieldInfo.getEntityPartitioner().getPartition(key));
			for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(key.getFields())){
				keyBuilder = codec.setKey(keyBuilder);
			}
			return Mutation.delete(tableName, keyBuilder.build());
		}

	}

	private class EntityListOp extends SpannerBaseReadOp<EK>{

		private final Integer partition;
		private final EK startKey;
		private final boolean startInclusive;

		public EntityListOp(
				DatabaseClient client,
				Config config,
				SpannerFieldCodecRegistry codecRegistry,
				String tableName,
				Integer partition,
				EK startKey,
				boolean startInclusive){
			super(client, config, codecRegistry, tableName);
			this.partition = partition;
			this.startKey = startKey;
			this.startInclusive = startInclusive;
		}

		@Override
		public KeySet buildKeySet(){
			KeySet.Builder keySetBuilder = KeySet.newBuilder();
			var rangeBuilder = KeyRange.newBuilder().setStart(primaryKeyConversion(startKey));
			rangeBuilder.setStartType(startInclusive ? Endpoint.CLOSED : Endpoint.OPEN);
			keySetBuilder.addRange(rangeBuilder.build());
			return keySetBuilder.build();
		}

		@Override
		public List<EK> wrappedCall(){
			return callClient(
					entityFieldInfo.getEntityKeySupplier().get().getFieldNames(),
					entityFieldInfo.getEntityKeyFields(),
					entityFieldInfo.getEntityKeySupplier());
		}

		protected Key primaryKeyConversion(EK key){
			Builder mutationKey = Key.newBuilder();
			mutationKey.append(partition);
			if(key == null){
				return mutationKey.build();
			}
			for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(key.getFields())){
				if(codec.getField().getValue() == null){
					continue;
				}
				mutationKey = codec.setKey(mutationKey);
			}
			return mutationKey.build();
		}

	}

}
