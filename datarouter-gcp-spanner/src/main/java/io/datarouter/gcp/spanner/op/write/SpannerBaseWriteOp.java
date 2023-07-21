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
package io.datarouter.gcp.spanner.op.write;

import java.util.Collection;
import java.util.Optional;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.SpannerException;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.exception.DatarouterInsertOrBustException;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;

public abstract class SpannerBaseWriteOp<T> extends SpannerBaseOp<Void>{

	private static final Integer DEFAULT_BATCH_SIZE = 100;

	protected final DatabaseClient client;
	protected final Optional<Integer> batchSize;

	protected final String tableName;
	protected final Collection<T> values;
	private final PutMethod putMethod;

	public SpannerBaseWriteOp(
			DatabaseClient client,
			String tableName,
			Config config,
			Collection<T> values){
		super("SpannerWrite: " + tableName);
		this.client = client;
		this.batchSize = config.findRequestBatchSize();
		this.tableName = tableName;
		this.values = values;
		this.putMethod = config.getPutMethod();
	}

	@Override
	public Void wrappedCall(){
		// Add trace
		Collection<Mutation> mutations = getMutations();

		boolean isPutIgnore = putMethod == PutMethod.INSERT_IGNORE;

		Scanner.of(mutations)
				.batch(isPutIgnore ? 1 : batchSize.orElse(DEFAULT_BATCH_SIZE))
				.forEach(mutationBatch -> {
					try{
						client.write(mutationBatch);
					}catch(Exception ex){
						String dataDesc;
						if(mutationBatch.size() > 5){
							dataDesc = "mutationBatchSize=" + mutationBatch.size();
						}else{
							dataDesc = "data=" + values;
						}
						if(ex instanceof SpannerException spannerEx
								&& spannerEx.getErrorCode() == ErrorCode.ALREADY_EXISTS){
							if(isPutIgnore){
								return;
							}
							String message = String.format("error inserting table=%s", tableName);
							throw new DatarouterInsertOrBustException(message, spannerEx);
						}
						String message = String.format("Error writing data to table=%s with %s", tableName, dataDesc);
						throw new DataAccessException(message, ex);
					}
				});
		return null;
	}

	public abstract Collection<Mutation> getMutations();

}
