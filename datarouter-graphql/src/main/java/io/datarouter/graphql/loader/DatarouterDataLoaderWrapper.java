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
package io.datarouter.graphql.loader;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import org.dataloader.MappedBatchLoader;

import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.util.GraphQlCounters;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.trace.TracerTool;

public class DatarouterDataLoaderWrapper<K extends DataLoaderKey,V> implements MappedBatchLoader<K,GraphQlResultDto<V>>{

	private final DatarouterBatchLoader<K,V> batchedService;
	private final Executor executor;

	public static <K extends DataLoaderKey,V> DatarouterDataLoaderWrapper<K,V> wrap(
			DatarouterBatchLoader<K,V> batchedService,
			Executor executor){
		return new DatarouterDataLoaderWrapper<>(batchedService, executor);
	}

	private DatarouterDataLoaderWrapper(DatarouterBatchLoader<K,V> batchedService, Executor executor){
		this.batchedService = batchedService;
		this.executor = executor;
	}

	@Override
	public CompletionStage<Map<K,GraphQlResultDto<V>>> load(Set<K> keys){
		Metrics.measure(GraphQlCounters.DATA_BATCH_LOADER + " batchSize " + batchedService.getClass().getSimpleName(),
				keys.size());
		return CompletableFuture.supplyAsync(() -> {
			try(var $ = TracerTool.startSpanNoGroupType(batchedService.getClass().getSimpleName())){
				return batchedService.load(keys);
			}
		}, executor);
	}

}
