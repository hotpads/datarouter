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
package io.datarouter.metric.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.node.op.index.IndexReader;
import io.datarouter.storage.node.op.index.IndexUsage;
import io.datarouter.storage.node.op.index.IndexUsage.IndexUsageType;
import io.datarouter.storage.node.op.index.MultiIndexReader;
import io.datarouter.storage.node.op.index.UniqueIndexReader;
import io.datarouter.storage.node.op.index.UniqueIndexWriter;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.indexusage.IndexUsageBuilder;
import io.datarouter.web.indexusage.IndexUsageBuilder.IndexUsageQueryItemRequestDto;
import io.datarouter.web.indexusage.IndexUsageBuilder.IndexUsageQueryItemResponseDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class IndexUsageService{

	@Inject
	private DaoClasses daoClasses;
	@Inject
	private ServiceName serviceName;
	@Inject
	private IndexUsageBuilder indexUsageBuilder;

	public List<IndexUsageQueryItemResponseDto> getAllIndexUsageMetrics(
			DatarouterDuration window,
			String username,
			Threads threads){
		return indexUsageBuilder.getIndexUsage(
				generateQueryItemDtos(),
				serviceName.get(),
				window,
				username,
				threads);
	}

	public List<IndexUsageQueryItemResponseDto> getActionableIndexes(Long daysToQuery){
		return Scanner.of(
						getAllIndexUsageMetrics(new DatarouterDuration(daysToQuery, TimeUnit.DAYS),
								getClass().getSimpleName(),
								Threads.none()))
				.include(index -> index.usageType() == IndexUsageType.IN_USE)
				.include(index -> index.count() == 0)
				.list();
	}

	public String buildIndexMetricName(String indexName){
		return indexUsageBuilder.buildIndexMetricName(indexName);
	}

	public List<IndexMapping> getIndexNames(List<Class<? extends Dao>> daoClasses){
		return Scanner.of(daoClasses)
				.concat(clazz -> Scanner.of(clazz.getDeclaredFields()))
				.include(field ->
						UniqueIndexReader.class.isAssignableFrom(field.getType())
								|| IndexReader.class.isAssignableFrom(field.getType())
								|| MultiIndexReader.class.isAssignableFrom(field.getType())
								|| UniqueIndexWriter.class.isAssignableFrom(field.getType()))
				.map(field -> {
					Type genericFieldType = field.getGenericType();
					if(genericFieldType instanceof ParameterizedType paramType){
						// Get the type arguments (the generic parameters)
						Type[] typeArguments = paramType.getActualTypeArguments();
						if(typeArguments.length >= 3){
							Class<?> indexNameClass = (Class<?>)typeArguments[2];
							IndexUsage usage = indexNameClass.getAnnotation(IndexUsage.class);
							IndexUsageType usageType = usage == null ? IndexUsageType.IN_USE : usage.usageType();
							return new IndexMapping(indexNameClass.getSimpleName(), usageType);
						}
					}
					return null;
				})
				.exclude(Objects::isNull)
				.distinct()
				.sort(Comparator.comparing(IndexMapping::indexName))
				.list();
	}

	private List<IndexMapping> getIndexNames(){
		return getIndexNames(daoClasses.get());
	}

	public record IndexMapping(
			String indexName,
			IndexUsageType usageType){
	}

	private List<IndexUsageQueryItemRequestDto> generateQueryItemDtos(){
		return Scanner.of(getIndexNames())
				.map(indexMapping -> new IndexUsageQueryItemRequestDto(indexMapping.indexName, indexMapping.usageType,
						UUID.randomUUID().toString()))
				.list();
	}

}
