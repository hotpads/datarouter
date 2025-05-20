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
package io.datarouter.web.handler.documentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerTool;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;
import io.datarouter.web.handler.types.HandlerDecoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ApiDocSchemaService{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private ApiDocTypeOverrides apiDocOverrides;

	public List<ApiDocSchemaDto> buildSchemas(String apiUrlContext, List<RouteSet> routeSets){
		List<DispatchRule> rules = Scanner.of(routeSets)
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.list();
		Map<String,ApiDocSchemaDto> currentSchemas = new HashMap<>();
		rules.forEach(rule -> this.buildSchemas(rule, currentSchemas));
		return Scanner.of(currentSchemas.values())
				.sort(Comparator.comparing(ApiDocSchemaDto::toSimpleClassName))
				.list();
	}

	private void buildSchemas(DispatchRule rule, Map<String,ApiDocSchemaDto> currentSchemas){
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for(Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				Handler handlerAnnotation = method.getAnnotation(Handler.class);
				String url = rule.getPattern().pattern();
				if(!url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)
						&& !url.substring(url.lastIndexOf('/') + 1).equals(method.getName())
						&& !handlerAnnotation.defaultHandler()){
					continue;
				}
				if(url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)){
					String urlSuffix = handlerAnnotation.defaultHandler() ? "" : "/" + method.getName();
					url = url.replace(BaseRouteSet.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				}
				Type responseType = method.getGenericReturnType();
				if(responseType == Void.TYPE){
					continue;
				}
				JsonAwareHandlerCodec jsonDecoder = null;
				Class<? extends HandlerDecoder> decoderClass = HandlerTool.getHandlerDecoderClass(
						handlerAnnotation,
						rule);
				if(JsonAwareHandlerCodec.class.isAssignableFrom(decoderClass)){
					jsonDecoder = (JsonAwareHandlerCodec)injector.getInstance(decoderClass);
				}
				Map<Class<?>, String> typeOverrides = apiDocOverrides.getOverrides();
				ApiDocSchemaDto schema = ApiDocSchemaTool.buildSchemaFromType(responseType, jsonDecoder, typeOverrides);
				buildSchemas(schema, currentSchemas);
				boolean isBaseEndpointObject = EndpointTool.paramIsBaseEndpointObject(method);
				if(isBaseEndpointObject){
					BaseEndpoint baseEndpoint = ReflectionTool.createWithoutNoArgs(
							(Class<? extends BaseEndpoint>)method.getParameters()[0].getType());
					List<Field> endpointFields = Scanner.of(baseEndpoint.getClass().getFields())
							.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
							.include(field -> field.isAnnotationPresent(RequestBody.class))
							.list();
					for(Field field : endpointFields){
						ApiDocSchemaDto requestBodySchema =
								ApiDocSchemaTool.buildSchemaFromType(field.getGenericType(),
								jsonDecoder, typeOverrides);
						buildSchemas(requestBodySchema, currentSchemas);

					}
				}else{
					Parameter[] parameters = method.getParameters();
					for(Parameter parameter : parameters){
						if(parameter.isAnnotationPresent(RequestBody.class)){
							ApiDocSchemaDto requestBodySchema =
									ApiDocSchemaTool.buildSchemaFromType(parameter.getParameterizedType(),
											jsonDecoder, typeOverrides);
							buildSchemas(requestBodySchema, currentSchemas);
						}
					}
				}
			}
			handler = handler.getSuperclass().asSubclass(BaseHandler.class);
		}
	}

	private void buildSchemas(ApiDocSchemaDto root, Map<String, ApiDocSchemaDto> currentSchemas){
		ApiDocSchemaTool.buildAllSchemas(root, currentSchemas);
	}
}
