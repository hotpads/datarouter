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
package io.datarouter.graphql.playground;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.graphql.client.util.config.Ql;
import io.datarouter.graphql.client.util.query.GraphQlClientTool;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.client.util.type.GraphQlType;
import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.graphql.service.GraphQlSchemaService.EmptyGraphQlArgumentType;
import io.datarouter.graphql.tool.GraphQlTool;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlPlaygroundSampleService{

	public static final String SAMPLE_INFO
			= "# This is a sample query, please remove or add more stuff in the editor.\n";

	@Inject
	private DatarouterInjector injector;

	public Map<GraphQlRootType,Map<String,String>> buildSchemaQuerySamples(
			Class<? extends GraphQlBaseHandler> handlerClass){
		GraphQlBaseHandler handler = injector.getInstance(handlerClass);
		GraphQlFetcherRegistry fetcherRegistry = injector.getInstance(handler.fetcherRegistry());
		Map<GraphQlRootType,List<RootQueryData>> fetchersByType = getRootQueryDataByType(handler, fetcherRegistry);
		return Scanner.of(fetchersByType.entrySet())
				.toMap(Entry::getKey, entry -> buildSampleQueries(entry.getValue(), entry.getKey(), fetcherRegistry));
	}

	private Map<GraphQlRootType,List<RootQueryData>> getRootQueryDataByType(
			GraphQlBaseHandler handler,
			GraphQlFetcherRegistry fetcherRegistry){
		Class<? extends GraphQlType> qlClazz = handler.getRootType();
		Map<GraphQlRootType,List<RootQueryData>> rootQueryDataByType = new HashMap<>();
		for(Field field : qlClazz.getFields()){
			if(field.isAnnotationPresent(Ql.class)){
				GraphQlRootType rootType = field.getAnnotation(Ql.class).root();
				if(rootType != GraphQlRootType.NONE){
					List<RootQueryData> rootQueries = rootQueryDataByType.getOrDefault(rootType, new ArrayList<>());
					Class<?> returnTypeClass = getTypeClassFromListOrField(field);
					String fetcherName = field.getAnnotation(Ql.class).fetcherId();
					Optional<Class<? extends DatarouterDataFetcher<?,?>>> fetcherClass = fetcherRegistry.find(
							fetcherName);
					rootQueries.add(new RootQueryData(field.getName(), fetcherClass.get(), returnTypeClass));
					rootQueryDataByType.put(rootType, rootQueries);
				}
			}
		}
		return rootQueryDataByType;
	}

	private Map<String,String> buildSampleQueries(
			List<RootQueryData> rootQueryData,
			GraphQlRootType rootQueryType,
			GraphQlFetcherRegistry registry){
		return rootQueryData.stream()
				.collect(Collectors.toMap(
						data -> data.queryName,
						data -> SAMPLE_INFO
								+ rootQueryType.getPersistentString()
								+ "{"
								+ data.queryName
								+ buildArgumentString(data.fetcherClass)
								+ buildQueryFromQlType(data.returnClass, registry)
								+ "}"));
	}

	private String buildArgumentString(Class<? extends DatarouterDataFetcher<?,?>> fetcher){
		Class<? extends GraphQlArgumentType> argumentClass = GraphQlTool.getArgumentClassFromFetcherClass(fetcher);
		if(argumentClass.equals(EmptyGraphQlArgumentType.class)){
			return "";
		}
		GraphQlArgumentType sample = ReflectionTool.createNullArgsWithUnsafeAllocator(argumentClass).getSample();
		if(sample == null){
			throw new IllegalStateException("Please provide an sample GraphQlArgumentsDto for " + argumentClass
					.getSimpleName());
		}
		return GraphQlClientTool.buildArgumentString(sample);
	}

	private Object buildQueryFromQlType(Class<?> typeClass, GraphQlFetcherRegistry registry){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(Field field : typeClass.getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			sb.append(field.getName());
			if(field.isAnnotationPresent(Ql.class)){
				String fetcherName = field.getAnnotation(Ql.class).fetcherId();
				Optional<Class<? extends DatarouterDataFetcher<?,?>>> fetcherClassOpt = registry.find(fetcherName);
				fetcherClassOpt.ifPresent(fetcherClass -> sb.append(buildArgumentString(fetcherClass)));
			}
			Class<?> clazz = getTypeClassFromListOrField(field);
			if(GraphQlType.class.isAssignableFrom(clazz)){
				sb.append(buildQueryFromQlType(clazz, registry));
			}
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	private static Class<?> getTypeClassFromListOrField(Field field){
		if(List.class.isAssignableFrom(field.getType())){
			ParameterizedType type = (ParameterizedType)field.getGenericType();
			return (Class<?>)type.getActualTypeArguments()[0];
		}
		return field.getType();
	}

	private static class RootQueryData{

		public final String queryName;
		public final Class<? extends DatarouterDataFetcher<?,?>> fetcherClass;
		public final Class<?> returnClass;

		private RootQueryData(
				String name,
				Class<? extends DatarouterDataFetcher<?,?>> fetcherClass,
				Class<?> returnClass){
			this.queryName = name;
			this.fetcherClass = fetcherClass;
			this.returnClass = returnClass;
		}

	}

}
