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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.cloud.spanner.DatabaseClient;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

public class SpannerDeleteUniqueOp<
		PK extends PrimaryKey<PK>,
		K extends UniqueKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseIndexDelete<PK,D,F,K>{

	public SpannerDeleteUniqueOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<K> keys,
			Config config,
			SpannerFieldCodecRegistry codecRegistry){
		super(client, fieldInfo, keys, config, codecRegistry);
	}

	@Override
	protected String getIndexName(K key){
		List<String> indexFields = key.getFieldNames();
		for(Entry<String,List<Field<?>>> uniqueIndex : fieldInfo.getUniqueIndexes().entrySet()){
			List<String> fieldNames = IterableTool.nullSafeMap(uniqueIndex.getValue(), field -> field.getKey()
					.getName());
			if(ListTool.compare(indexFields, fieldNames) == 0){
				return uniqueIndex.getKey();
			}
		}
		throw new RuntimeException("Cannot find index Name for key");
	}
}
