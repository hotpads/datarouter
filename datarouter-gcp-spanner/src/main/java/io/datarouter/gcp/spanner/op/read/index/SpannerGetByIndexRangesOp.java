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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerGetByIndexRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>>
extends SpannerBaseReadIndexOp<PK,D>{

	private final Collection<Range<IK>> ranges;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final String indexName;

	public SpannerGetByIndexRangesOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<IK>> ranges,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			String indexName){
		super(client, config, codecRegistry, fieldInfo.getTableName());
		this.ranges = ranges;
		this.fieldInfo = fieldInfo;
		this.indexName = indexName;
	}

	@Override
	public List<D> wrappedCall(){
		Integer offset = config.findOffset().orElse(0);
		ResultSet databeanRs;
		List<PK> keyList;
		List<D> databeans;
		try(ReadOnlyTransaction txn = client.readOnlyTransaction()){
			ResultSet rs;
			if(config.getLimit() != null){
				Integer limit = offset + config.getLimit();
				rs = txn.readUsingIndex(
						tableName,
						indexName,
						buildKeySet(),
						fieldInfo.getPrimaryKeyFieldColumnNames(),
						Options.limit(limit));
			}else{
				rs = txn.readUsingIndex(
						tableName,
						indexName,
						buildKeySet(),
						fieldInfo.getPrimaryKeyFieldColumnNames());
			}
			keyList = createFromResultSet(
					rs,
					fieldInfo.getPrimaryKeySupplier(),
					fieldInfo.getPrimaryKeyFields());
			if(config.getLimit() != null){
				databeanRs = txn.read(
						tableName,
						buildKeySet(keyList),
						fieldInfo.getFieldColumnNames(),
						Options.limit(config.getLimit()));
			}else{
				databeanRs = txn.read(tableName, buildKeySet(keyList), fieldInfo.getFieldColumnNames());
			}
			databeans = createFromResultSet(databeanRs, fieldInfo.getDatabeanSupplier(), fieldInfo.getFields());
		}
		Map<PK,D> databeanByKey = databeans.stream()
				.collect(Collectors.toMap(Databean::getKey, Function.identity()));
		List<D> sortedDatabeans = keyList.stream()
				.map(databeanByKey::get)
				.collect(Collectors.toList());
		if(offset > 0){
			return sortedDatabeans.subList(offset, sortedDatabeans.size());
		}
 		return sortedDatabeans;
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
