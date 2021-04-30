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
package io.datarouter.gcp.spanner.op.write;

import java.util.Collection;
import java.util.Optional;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;

public abstract class SpannerBaseWriteOp<T> extends SpannerBaseOp<Void>{

	private static final Integer DEFAULT_BATCH_SIZE = 1000;

	protected final DatabaseClient client;
	protected final Optional<Integer> batchSize;

	protected final String tableName;
	protected final Collection<T> values;

	public SpannerBaseWriteOp(
			DatabaseClient client,
			String tableName,
			Config config,
			Collection<T> values){
		super("SpannerWrite: " + tableName);
		this.client = client;
		this.batchSize = Optional.ofNullable(config.getCommitBatchSize());
		this.tableName = tableName;
		this.values = values;
	}

	@Override
	public Void wrappedCall(){
		// Add trace
		Collection<Mutation> mutations = getMutations();
		Scanner.of(mutations)
				.batch(batchSize.orElse(DEFAULT_BATCH_SIZE))
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
						throw new DataAccessException("Error writing data to table=" + tableName + " with " + dataDesc,
								ex);
					}
				});
		return null;
	}

	public abstract Collection<Mutation> getMutations();

}
