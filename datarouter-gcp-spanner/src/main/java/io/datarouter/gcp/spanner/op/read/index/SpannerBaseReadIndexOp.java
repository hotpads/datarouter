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

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.read.SpannerBaseReadOp;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;

public abstract class SpannerBaseReadIndexOp<PK extends PrimaryKey<PK>,T>
extends SpannerBaseReadOp<T>{

	public SpannerBaseReadIndexOp(
			DatabaseClient client,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			String tableName){
		super(client, config, codecRegistry, tableName);
	}

	@SuppressWarnings("unchecked")
	protected Key indexKeyConversion(PrimaryKey<?> key){
		return primaryKeyConversion((PK) key);
	}

	protected KeySet buildKeySet(Collection<? extends PrimaryKey<?>> values){
		KeySet.Builder keySetBuilder = KeySet.newBuilder();
		values.stream()
				.map(this::indexKeyConversion)
				.forEach(keySetBuilder::addKey);
		return keySetBuilder.build();
	}

}
