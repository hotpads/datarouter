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
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.KeyRange;
import com.google.cloud.spanner.KeyRange.Endpoint;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.SpannerException;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.util.tuple.Range;

public abstract class SpannerBaseReadOp<T> extends SpannerBaseOp<List<T>>{
	private static final Logger logger = LoggerFactory.getLogger(SpannerBaseReadOp.class);

	protected final DatabaseClient client;
	protected final Config config;
	protected final SpannerFieldCodecs fieldCodecs;
	protected final String tableName;

	public SpannerBaseReadOp(
			DatabaseClient client,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			String tableName){
		//TODO implement traces
		super("Spanner read");
		this.client = client;
		this.config = config;
		this.fieldCodecs = fieldCodecs;
		this.tableName = tableName;
	}

	public abstract KeySet buildKeySet();

	protected <K extends PrimaryKey<K>> Key primaryKeyConversion(K key){
		if(key == null){
			return Key.of();
		}
		Builder mutationKey = Key.newBuilder();
		for(SpannerBaseFieldCodec<?,?> codec : fieldCodecs.createCodecs(key.getFields())){
			if(codec.getField().getValue() == null){
				break;
			}
			mutationKey = codec.setKey(mutationKey);
		}
		return mutationKey.build();
	}

	protected <F> List<F> callClient(List<String> columnNames, List<Field<?>> fields, Supplier<F> object){
		String spanName = getClass().getSimpleName();
		try(var _ = TracerTool.startSpan(spanName, TraceSpanGroupType.DATABASE)){
			List<F> results = callClientInternal(columnNames, fields, object);
			TracerTool.appendToSpanInfo("got " + results.size());
			return results;
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
		ReadOption[] readOptions = config.findLimit()
				.map(limit -> new ReadOption[]{Options.limit(offset + limit)})
				.orElseGet(() -> new ReadOption[]{});
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

	protected <F> List<F> createFromResultSet(ResultSet rs, Supplier<F> objectSupplier, List<Field<?>> fields){
		List<? extends SpannerBaseFieldCodec<?,?>> codecs = fieldCodecs.createCodecs(fields);
		List<F> objects = new ArrayList<>();
		int resultCounter = 0;//for debugging session leak; see if we're before the first result
		try{
			while(rs.next()){
				++resultCounter;
				F object = objectSupplier.get();
				codecs.forEach(codec -> codec.setField(object, rs));
				objects.add(object);
			}
			return objects;
		}catch(SpannerException e){
			logger.warn("resultCounter={}", resultCounter);
			/*
			 * This happens when calling spanner in a thread whose Future times out.
			 *
			 * Clear the interrupted flag, undoing the work of SpannerExceptionFactory.propagateInterrupt
			 *
			 * Potential spanner java client bug?  Or hack to fix unidentified datarouter bug?
			 *
			 * It appears that with or without this the PooledSessionFuture errors during its close() method, but by
			 * clearing the interrupted flag the session isn't leaked.
			 */
			if(e.getErrorCode().equals(ErrorCode.CANCELLED)){
				Thread.interrupted();
			}
			throw e;
		}
	}

}
