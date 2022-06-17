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
package io.datarouter.gcp.spanner.scan;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.read.SpannerGetKeyRangesOp;
import io.datarouter.gcp.spanner.op.read.SpannerGetKeyRangesSqlOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerKeyScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNodeScanner<PK,PK>{

	private final DatabaseClient client;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final SpannerFieldCodecs fieldCodecs;

	public SpannerKeyScanner(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			boolean caseInsensitive){
		super(ranges, config, caseInsensitive);
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.fieldCodecs = fieldCodecs;
	}

	@Override
	protected PK getPrimaryKey(PK fieldSet){
		return fieldSet;
	}

	@Override
	protected List<PK> loadRanges(Collection<Range<PK>> ranges, Config config){
		return config.findOffset().orElse(0) > 0
				? new SpannerGetKeyRangesSqlOp<>(client, fieldInfo, ranges, config, fieldCodecs).wrappedCall()
				: new SpannerGetKeyRangesOp<>(client, fieldInfo, ranges, config, fieldCodecs).wrappedCall();
	}

}
