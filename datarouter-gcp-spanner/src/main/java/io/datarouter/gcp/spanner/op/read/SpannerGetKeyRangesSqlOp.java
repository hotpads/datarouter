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
package io.datarouter.gcp.spanner.op.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.gcp.spanner.sql.SpannerSql;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerGetKeyRangesSqlOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<List<PK>>{

	private final DatabaseClient client;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final Collection<Range<PK>> ranges;
	private final Config config;
	private final SpannerFieldCodecs fieldCodecs;

	public SpannerGetKeyRangesSqlOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecs fieldCodecs){
		super(SpannerGetKeyRangesSqlOp.class.getSimpleName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.ranges = ranges;
		this.config = config;
		this.fieldCodecs = fieldCodecs;
	}

	@Override
	public List<PK> wrappedCall(){
		String spanName = getClass().getSimpleName();
		try(var $ = TracerTool.startSpan(spanName, TraceSpanGroupType.DATABASE)){
			List<PK> results = wrappedCallInternal();
			TracerTool.appendToSpanInfo("offset " + config.findOffset().orElse(0));
			TracerTool.appendToSpanInfo("got " + results.size());
			return results;
		}
	}

	private List<PK> wrappedCallInternal(){
		Statement statement = new SpannerSql(fieldCodecs)
				.getInRanges(
						fieldInfo.getTableName(),
						config,
						fieldInfo.getPrimaryKeyFields(),
						ranges,
						fieldInfo.getPrimaryKeyFields(),
						null)
				.prepare(null)
				.build();
		ResultSet resultSet = client.singleUseReadOnlyTransaction().executeQuery(statement);
		return parseResultSet(resultSet);
	}

	private List<PK> parseResultSet(ResultSet resultSet){
		List<? extends SpannerBaseFieldCodec<?,?>> codecs = fieldCodecs.createCodecs(fieldInfo.getPrimaryKeyFields());
		List<PK> objects = new ArrayList<>();
		while(resultSet.next()){
			PK object = fieldInfo.getPrimaryKeySupplier().get();
			codecs.forEach(codec -> codec.setField(object, resultSet));
			objects.add(object);
		}
		return objects;
	}

}