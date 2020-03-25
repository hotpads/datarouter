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
package io.datarouter.gcp.spanner.scan.entity;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.entity.read.SpannerEntityGetKeyRangesOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerEntityKeyScanner<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNodeScanner<PK,PK>{

	private final DatabaseClient client;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final SpannerFieldCodecRegistry codecRegistry;
	private final Integer parition;

	public SpannerEntityKeyScanner(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			boolean caseInsensitive,
			Integer partition){
		super(ranges, config, caseInsensitive);
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.codecRegistry = codecRegistry;
		this.parition = partition;
	}

	@Override
	protected PK getPrimaryKey(PK fieldSet){
		return fieldSet;
	}

	@Override
	protected List<PK> loadRanges(Collection<Range<PK>> ranges, Config config){
		var op = new SpannerEntityGetKeyRangesOp<>(client, fieldInfo, ranges, config, codecRegistry, parition);
		return op.wrappedCall();
	}

}
