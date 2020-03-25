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
package io.datarouter.gcp.spanner.op.entity.write;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Mutation.WriteBuilder;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.node.entity.SpannerSubEntityNode;
import io.datarouter.gcp.spanner.op.write.SpannerPutOp;
import io.datarouter.gcp.spanner.util.SpannerEntityKeyTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

public class SpannerEntityPutOp<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerPutOp<PK,D,F>{

	private final EntityPartitioner<EK> partitioner;

	public SpannerEntityPutOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<D> databeans,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			EntityPartitioner<EK> partitioner){
		super(client, fieldInfo, databeans, config, codecRegistry, SpannerEntityKeyTool.getEntityTableName(fieldInfo));
		this.partitioner = partitioner;
	}

	@Override
	public Collection<Mutation> getMutations(){
		var entityMutations = IterableTool.map(values, this::createEntityRow);
		var databeanMutations = IterableTool.map(values, this::databeanToMutation);
		return ListTool.concatenate(entityMutations, databeanMutations);

	}

	private Mutation createEntityRow(D databean){
		List<SpannerBaseFieldCodec<?,?>> entityCodecs = codecRegistry.createCodecs(databean.getKey()
				.getEntityKeyFields());
		WriteBuilder mutation = Mutation.newInsertOrUpdateBuilder(fieldInfo.getTableName());
		mutation = getMutationPartition(databean, mutation);
		for(var codec : entityCodecs){
			if(codec.getField().getValue() != null){
				mutation = codec.setMutation(mutation);
			}
		}
		return mutation.build();
	}

	@Override
	protected WriteBuilder getMutationPartition(D databean, WriteBuilder mutation){
		return mutation.set(SpannerSubEntityNode.PARTITION_COLUMN_NAME).to(partitioner.getPartition(databean.getKey()
				.getEntityKey()));
	}

}
