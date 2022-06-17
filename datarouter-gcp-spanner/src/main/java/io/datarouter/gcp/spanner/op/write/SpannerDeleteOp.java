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
package io.datarouter.gcp.spanner.op.write;

import java.util.Collection;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.Mutation;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerDeleteOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseWriteOp<PK>{

	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	protected final SpannerFieldCodecs fieldCodecs;

	public SpannerDeleteOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<PK> keys,
			Config config,
			SpannerFieldCodecs fieldCodecs){
		this(client, fieldInfo, keys, config, fieldCodecs, fieldInfo.getTableName());
	}

	protected SpannerDeleteOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<PK> keys,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			String tableName){
		super(client, tableName, config, keys);
		this.fieldInfo = fieldInfo;
		this.fieldCodecs = fieldCodecs;
	}

	@Override
	public Collection<Mutation> getMutations(){
		return Scanner.of(values).map(this::keyToDeleteMutation).list();
	}

	protected Mutation keyToDeleteMutation(PK key){
		Builder mutationKey = Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : fieldCodecs.createCodecs(key.getFields())){
			mutationKey = codec.setKey(mutationKey);
		}
		return Mutation.delete(tableName, mutationKey.build());
	}

}
