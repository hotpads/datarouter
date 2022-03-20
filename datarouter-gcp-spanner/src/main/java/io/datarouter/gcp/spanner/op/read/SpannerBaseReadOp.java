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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.KeyRange;
import com.google.cloud.spanner.KeyRange.Endpoint;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.opencensus.adapter.DatarouterOpencensusTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.util.tuple.Range;
import io.opencensus.common.Scope;

public abstract class SpannerBaseReadOp<T> extends SpannerBaseOp<List<T>>{

	protected final DatabaseClient client;
	protected final Config config;
	protected final SpannerFieldCodecRegistry codecRegistry;
	protected final String tableName;

	public SpannerBaseReadOp(
			DatabaseClient client,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			String tableName){
		//TODO implement traces
		super("Spanner read");
		this.client = client;
		this.config = config;
		this.codecRegistry = codecRegistry;
		this.tableName = tableName;
	}

	public abstract KeySet buildKeySet();

	protected <K extends PrimaryKey<K>> Key primaryKeyConversion(K key){
		if(key == null){
			return Key.of();
		}
		Builder mutationKey = Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : codecRegistry.createCodecs(key.getFields())){
			if(codec.getField().getValue() == null){
				break;
			}
			mutationKey = codec.setKey(mutationKey);
		}
		return mutationKey.build();
	}

	protected <F> List<F> callClient(List<String> columnNames, List<Field<?>> fields, Supplier<F> object){
		String spanName = getClass().getSimpleName();
		Optional<Scope> opencensusSpan = Optional.empty();
		try(var $ = TracerTool.startSpan(spanName, TraceSpanGroupType.DATABASE)){
			opencensusSpan = DatarouterOpencensusTool.createOpencensusSpan();
			List<F> results = callClientInternal(columnNames, fields, object);
			TracerTool.appendToSpanInfo("got " + results.size());
			return results;
		}finally{
			opencensusSpan.ifPresent(Scope::close);
		}
	}

	/*
	 * Note about ReadContext.read(..)
	 *  "any SpannerException is deferred to the first or subsequent ResultSet#next() call"
	 * So it would happen inside createFromResultSet(..)
	 */
	private <F> List<F> callClientInternal(List<String> columnNames, List<Field<?>> fields, Supplier<F> object){
		KeySet keySet = buildKeySet();
		int offset = config.findOffset().orElse(0);
		ReadOption[] readOptions = makeReadOptions(offset, config);
		try(ReadContext txn = client.singleUseReadOnlyTransaction()){
			try(ResultSet rs = txn.read(tableName, keySet, columnNames, readOptions)){
				List<F> results = createFromResultSet(rs, object, fields);
				if(offset >= results.size()){
					return List.of();
				}
				if(offset > 0){
					int size = results.size() - offset;
					return Scanner.of(results)
							.skip(offset)
							.collect(() -> new ArrayList<>(size));
				}
				return results;
			}
		}
	}

	private static ReadOption[] makeReadOptions(int offset, Config config){
		return config.getLimit() == null
				? new ReadOption[]{}
				: new ReadOption[]{Options.limit(offset + config.getLimit())};
	}

	protected <K extends PrimaryKey<K>> KeyRange convertRange(Range<K> range){
		KeyRange.Builder builder = KeyRange.newBuilder()
				.setStart(primaryKeyConversion(range.getStart()))
				.setEnd(primaryKeyConversion(range.getEnd()));
		if(range.isEmptyStart()){
			builder.setStartType(Endpoint.CLOSED);
		}else{
			builder.setStartType(range.getStartInclusive() ? Endpoint.CLOSED : Endpoint.OPEN);
		}
		if(range.isEmptyEnd()){
			builder.setEndType(Endpoint.CLOSED);
		}else{
			builder.setEndType(range.getEndInclusive() ? Endpoint.CLOSED : Endpoint.OPEN);
		}
		return builder.build();
	}

	protected <F> List<F> createFromResultSet(ResultSet set, Supplier<F> objectSupplier, List<Field<?>> fields){
		List<? extends SpannerBaseFieldCodec<?,?>> codecs = codecRegistry.createCodecs(fields);
		List<F> objects = new ArrayList<>();
		while(set.next()){
			F object = objectSupplier.get();
			codecs.forEach(codec -> codec.setField(object, set));
			objects.add(object);
		}
		return objects;
	}

}
