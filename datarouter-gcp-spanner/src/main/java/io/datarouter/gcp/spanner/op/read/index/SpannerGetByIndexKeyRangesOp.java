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
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerGetByIndexKeyRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends SpannerBaseReadIndexOp<PK,IK>{

	private final Collection<Range<IK>> ranges;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;

	public SpannerGetByIndexKeyRangesOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		super(client, config, fieldCodecs, fieldInfo.getTableName());
		this.ranges = ranges;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
	}

	@Override
	public List<IK> wrappedCall(){
		String indexName = indexEntryFieldInfo.getIndexName();
		Integer offset = config.findOffset().orElse(0);
		ReadOption[] readOptions = config.findLimit()
				.map(limit -> new ReadOption[]{Options.limit(offset + limit)})
				.orElseGet(() -> new ReadOption[]{});
		try(ReadContext txn = client.singleUseReadOnlyTransaction()){
			try(ResultSet rs = txn.readUsingIndex(
					tableName,
					indexName,
					buildKeySet(),
					indexEntryFieldInfo.getPrimaryKeyFieldColumnNames(),
					readOptions)){
				List<IK> keyList = createFromResultSet(
						rs,
						indexEntryFieldInfo.getPrimaryKeySupplier(),
						indexEntryFieldInfo.getPrimaryKeyFields());
				if(offset > 0){
					//TODO don't return subList
					return keyList.subList(offset, keyList.size());
				}
				return keyList;
			}
		}
	}

	@Override
	public KeySet buildKeySet(){
		KeySet.Builder keySetBuilder = KeySet.newBuilder();
		if(ranges == null || ranges.isEmpty()){
			keySetBuilder.setAll();
		}else{
			ranges.stream()
					.map(this::convertRange)
					.forEach(keySetBuilder::addRange);
		}
		return keySetBuilder.build();
	}

}
