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
package io.datarouter.gcp.spanner.op.entity.read;

import java.util.Collection;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.entity.SpannerEntityOp;
import io.datarouter.gcp.spanner.op.read.SpannerGetRangesOp;
import io.datarouter.gcp.spanner.util.SpannerEntityKeyTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerEntityGetRangesOp<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerGetRangesOp<PK,D,F>
implements SpannerEntityOp{

	private final Integer partition;

	public SpannerEntityGetRangesOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			Integer partition){
		super(client, fieldInfo, ranges, config, codecRegistry, SpannerEntityKeyTool.getEntityTableName(fieldInfo));
		this.partition = partition;
	}

	@Override
	protected <K extends PrimaryKey<K>> Key primaryKeyConversion(K key){
		Builder mutationKey = Key.newBuilder();
		mutationKey.append(partition);
		if(key == null){
			return mutationKey.build();
		}
		for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(SpannerEntityKeyTool.getPrimaryKeyFields(key,
				fieldInfo.isSubEntity()))){
			if(codec.getField().getValue() == null){
				continue;
			}
			mutationKey = codec.setKey(mutationKey);
		}
		return mutationKey.build();
	}

}
