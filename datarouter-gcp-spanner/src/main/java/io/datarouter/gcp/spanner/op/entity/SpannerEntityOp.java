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
package io.datarouter.gcp.spanner.op.entity;

import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;

import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;

public interface SpannerEntityOp{

	@SuppressWarnings("unchecked")
	default <K extends PrimaryKey<K>,
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>>
	Builder getPartiton(K key, EntityPartitioner<EK> partitioner){
		PK entityKey = (PK) key;
		Builder keyBuilder = Key.newBuilder();
		keyBuilder.append(partitioner.getPartition(entityKey.getEntityKey()));
		return keyBuilder;
	}

}
