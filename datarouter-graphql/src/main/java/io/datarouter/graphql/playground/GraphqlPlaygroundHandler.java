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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.config.DatarouterGraphQlFiles;
import io.datarouter.graphql.service.GraphQlInstancesContainer;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.gson.GsonTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import jakarta.inject.Inject;

public class GraphqlPlaygroundHandler extends BaseHandler{

	public static final String PLAYGROUND_HEADER = "x-rg-requested";
	public static final Map<String,String> PLAYGROUND_HEADERS = Map.of(PLAYGROUND_HEADER, "graphql-playground");

	@Inject
	private DatarouterGraphQlFiles files;
	@Inject
	private GraphQlInstancesContainer container;

	@SuppressWarnings("deprecation")
	@Handler(defaultHandler = true)
	public Mav showJsp(Optional<String> rootName, Optional<String> queryName){
		List<GraphQlPlaygroundRootEndpointJspDto> playgroundJspDtos = new ArrayList<>();
		GraphQlPlaygroundRootEndpointJspDto defaultTab = null;
		for(Class<? extends GraphQlBaseHandler> handlerClass : container.getGraphQlHandlers()){
			for(Method method : handlerClass.getMethods()){
				if(method.isAnnotationPresent(Handler.class)){
					// Each handler contains only one endpoint but with different queries and queryTypes
					Map<GraphQlRootType,Map<String,String>> samples = container.getSchemaSampleQueries(handlerClass);
					List<GraphQlPlaygroundSampleQueryDto> sampleQueries = buildSampleQueries(samples);
					var jspDto = new GraphQlPlaygroundRootEndpointJspDto(
							container.getPath(handlerClass),
							method.getName(),
							sampleQueries);
					if(defaultTab == null){
						if(rootName.isPresent() && queryName.isPresent() && rootName.get().equals(method.getName())){
							// build default tab from request params
							defaultTab = buildDefaultTabFromRequestParams(
									container.getPath(handlerClass),
									method.getName(),
									queryName.get(),
									sampleQueries);
						}else if(rootName.isEmpty() || queryName.isEmpty()){
							defaultTab = jspDto;
						}
					}
					playgroundJspDtos.add(jspDto);
					break;
				}
			}
		}
		Mav mav = new Mav(files.jsp.graphql.playgroundJsp);
		mav.put("playgroundJspDtos", GsonTool.withUnregisteredEnums().toJson(playgroundJspDtos));
		mav.put("defaultQueryEndpoint", defaultTab.rootQueryUrl);
		mav.put("defaultTab", GsonTool.withUnregisteredEnums().toJson(defaultTab));
		return mav;
	}

	private static GraphQlPlaygroundRootEndpointJspDto buildDefaultTabFromRequestParams(
			String rootQueryUrl,
			String rootName,
			String requestedQuery,
			List<GraphQlPlaygroundSampleQueryDto> sampleQueries){
		Optional<GraphQlPlaygroundSampleQueryDto> queryDtoOpt = sampleQueries.stream()
				.filter(sample -> sample.name.equals(requestedQuery))
				.findFirst();
		return new GraphQlPlaygroundRootEndpointJspDto(rootQueryUrl, rootName, List.of(queryDtoOpt.get()));
	}

	private static List<GraphQlPlaygroundSampleQueryDto> buildSampleQueries(
			Map<GraphQlRootType,Map<String,String>> samples){
		List<GraphQlPlaygroundSampleQueryDto> dtos = new ArrayList<>();
		for(Entry<GraphQlRootType,Map<String,String>> sampleByTypeEntry : samples.entrySet()){
			GraphQlPlaygroundQueryTypeDto queryTypeDto = buildQueryTypeDto(sampleByTypeEntry.getKey());
			Map<String,String> sampleByQueryName = sampleByTypeEntry.getValue();
			Scanner.of(sampleByQueryName.entrySet())
					.map(entry -> new GraphQlPlaygroundSampleQueryDto(queryTypeDto, entry.getKey(), entry.getValue()))
					.flush(dtos::addAll);
		}
		return dtos;
	}

	private static GraphQlPlaygroundQueryTypeDto buildQueryTypeDto(GraphQlRootType type){
		return type == GraphQlRootType.QUERY
				? new GraphQlPlaygroundQueryTypeDto(true, false)
				: new GraphQlPlaygroundQueryTypeDto(false, true);
	}

	public static class GraphQlPlaygroundRootEndpointJspDto{

		public final String rootQueryUrl;
		public final String rootName;
		public final List<GraphQlPlaygroundSampleQueryDto> sampleQueries;
		public final Map<String,String> headers = PLAYGROUND_HEADERS;

		public GraphQlPlaygroundRootEndpointJspDto(
				String rootQueryUrl,
				String rootName,
				List<GraphQlPlaygroundSampleQueryDto> sampleQueries){
			this.rootQueryUrl = rootQueryUrl;
			this.rootName = rootName;
			this.sampleQueries = sampleQueries;
		}

	}

	public static class GraphQlPlaygroundSampleQueryDto{

		public final GraphQlPlaygroundQueryTypeDto queryTypes;
		public final String name;
		public final String query;

		public GraphQlPlaygroundSampleQueryDto(GraphQlPlaygroundQueryTypeDto queryTypes, String name, String query){
			this.queryTypes = queryTypes;
			this.name = name;
			this.query = query;
		}

	}

	public static class GraphQlPlaygroundQueryTypeDto{

		public final Boolean query;
		public final Boolean mutation;

		public GraphQlPlaygroundQueryTypeDto(Boolean query, Boolean mutation){
			this.query = query;
			this.mutation = mutation;
		}

	}

}
