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
import io.datarouter.gcp.spanner.op.read.index.SpannerGetByIndexKeyRangesOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.scan.BaseNodeScanner;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerByIndexKeyScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseNodeScanner<IK,IK>{

	private final DatabaseClient client;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final SpannerFieldCodecs fieldCodecs;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;

	public SpannerByIndexKeyScanner(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			boolean caseInsensitive){
		super(ranges, config, caseInsensitive);
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.fieldCodecs = fieldCodecs;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
	}

	@Override
	protected IK getPrimaryKey(IK fieldSet){
		return fieldSet;
	}

	@Override
	protected List<IK> loadRanges(Collection<Range<IK>> ranges, Config config){
		var getByIndexRangesKeyOp = new SpannerGetByIndexKeyRangesOp<>(client, fieldInfo, ranges, config, fieldCodecs,
				indexEntryFieldInfo);
		return getByIndexRangesKeyOp.wrappedCall();
	}

}
