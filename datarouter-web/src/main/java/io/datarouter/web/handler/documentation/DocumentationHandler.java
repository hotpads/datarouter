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
package io.datarouter.web.handler.documentation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.UnsafeAllocator;

import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.serialization.CompatibleDateTypeAdapter;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.handler.types.optional.OptionalParameter;

public class DocumentationHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DocumentationHandler.class);

	private static final Set<String> HIDDEN_SPEC_PARAMS = Set.of(
			SecurityParameters.CSRF_IV,
			SecurityParameters.CSRF_TOKEN,
			SecurityParameters.SIGNATURE);

	private static final UnsafeAllocator UNSAFE_ALLOCATOR = UnsafeAllocator.create();

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Date.class, new CompatibleDateTypeAdapter())
			.serializeNulls()
			.setPrettyPrinting()
			.create();

	@Inject
	private DatarouterWebFiles files;

	private List<DocumentedEndpointJspDto> buildDocumentation(String apiUrlContext, List<BaseRouteSet> routeSets){
		return routeSets.stream()
				.map(BaseRouteSet::getDispatchRules)
				.flatMap(Collection::stream)
				.filter(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.map(this::buildEndpointDocumentation)
				.flatMap(List::stream)
				.sorted(Comparator.comparing(DocumentedEndpointJspDto::getUrl))
				.collect(Collectors.toList());
	}

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, List<BaseRouteSet> routeSets){
		List<DocumentedEndpointJspDto> endpoints = buildDocumentation(apiUrlContext, routeSets);
		Mav model = new Mav(files.jsp.docs.dispatcherDocsJsp);
		model.put("endpoints", endpoints);
		model.put("apiName", apiName);
		model.put("hideAuth", false);
		return model;
	}

	private List<DocumentedEndpointJspDto> buildEndpointDocumentation(DispatchRule rule){
		List<DocumentedEndpointJspDto> endpoints = new ArrayList<>();
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for(Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				String urlSuffix = method.getAnnotation(Handler.class).defaultHandler() ? "" : "/" + method.getName();
				String url = rule.getPattern().pattern().replace(BaseRouteSet.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				List<DocumentedParameterJspDto> parameters = new ArrayList<>();
				String description = method.getAnnotation(Handler.class).description();
				parameters.addAll(createApplicableSecurityParameters(rule));
				parameters.addAll(createMethodParameters(method));

				Type responseType = method.getGenericReturnType();
				String responseExample;
				if(responseType == Void.TYPE){
					responseExample = null;
				}else{
					try{
						Object responseObject = createBestExample(responseType, new HashSet<>());
						responseExample = GSON.toJson(responseObject);
					}catch(Exception e){
						responseExample = "Impossible to render";
						logger.warn("Could not create response example for {}", responseType, e);
					}
				}
				Optional<Class<?>> clazz = responseType instanceof Class ? Optional.of((Class<?>)responseType)
						: Optional.empty();
				String responseTypeString = clazz.map(Class::getSimpleName).orElse(responseType.toString());
				DocumentedResponseJspDto response = new DocumentedResponseJspDto(responseTypeString, responseExample);
				DocumentedEndpointJspDto endpoint = new DocumentedEndpointJspDto(url, parameters, description,
						response);
				endpoints.add(endpoint);
			}
			handler = handler.getSuperclass().asSubclass(BaseHandler.class);
		}
		return endpoints;
	}

	private List<DocumentedParameterJspDto> createMethodParameters(Method method){
		List<DocumentedParameterJspDto> methodParameters = new ArrayList<>();
		Parameter[] parameters = method.getParameters();
		for(Parameter parameter : parameters){
			String description = null;
			String name = parameter.getName();
			Param param = parameter.getAnnotation(Param.class);
			if(param != null){
				description = param.description();
				if(!param.value().isEmpty()){
					name = param.value();
				}
			}
			methodParameters.add(createDocumentedParameter(name, parameter.getParameterizedType(),
					parameter.isAnnotationPresent(RequestBody.class), description));
		}
		return methodParameters;
	}

	private List<DocumentedParameterJspDto> createApplicableSecurityParameters(DispatchRule rule){
		List<String> applicableSecurityParameterNames = new ArrayList<>();
		if(rule.hasSignature()){
			applicableSecurityParameterNames.add(SecurityParameters.SIGNATURE);
		}
		if(rule.hasApiKey()){
			applicableSecurityParameterNames.add(SecurityParameters.API_KEY);
		}
		if(rule.hasCsrfToken()){
			applicableSecurityParameterNames.add(SecurityParameters.CSRF_TOKEN);
			applicableSecurityParameterNames.add(SecurityParameters.CSRF_IV);
		}
		return applicableSecurityParameterNames.stream()
				.map(parameterName -> createDocumentedParameter(parameterName, String.class, false, null))
				.collect(Collectors.toList());
	}

	private DocumentedParameterJspDto createDocumentedParameter(String parameterName, Type parameterType,
			boolean requestBody, String description){
		DocumentedParameterJspDto documentedParameter = new DocumentedParameterJspDto();
		documentedParameter.name = parameterName;
		Type type = OptionalParameter.getOptionalInternalType(parameterType);
		Optional<Class<?>> clazz = type instanceof Class ? Optional.of((Class<?>)type) : Optional.empty();
		documentedParameter.type = clazz.map(Class::getSimpleName).orElse(type.toString());
		try{
			if(!clazz.map(cls -> Number.class.isAssignableFrom(cls)).orElse(false)
					&& !clazz.map(cls -> String.class.isAssignableFrom(cls)).orElse(false)
					&& !clazz.map(cls -> Boolean.class.isAssignableFrom(cls)).orElse(false)
					&& !clazz.map(cls -> cls.isPrimitive()).orElse(false)){
				documentedParameter.example = GSON.toJson(createBestExample(type, new HashSet<>()));
			}
		}catch(Exception e){
			logger.warn("Could not create parameter example {} for {}", type, parameterName, e);
		}
		documentedParameter.required = !(parameterType instanceof Class) || !OptionalParameter.class.isAssignableFrom(
				(Class<?>)parameterType);
		documentedParameter.requestBody = requestBody;
		documentedParameter.description = description;
		documentedParameter.hidden = HIDDEN_SPEC_PARAMS.contains(parameterName);
		return documentedParameter;
	}

	private static Object createBestExample(Type type, Set<Type> parents){
		if(parents.contains(type)){
			return null;
		}
		if(type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Class<?> rawType = (Class<?>)parameterizedType.getRawType();
			if(List.class.isAssignableFrom(rawType)){
				return Arrays.asList(createBestExample(parameterizedType.getActualTypeArguments()[0], SetTool
						.concatenate(parents, type)));
			}
			if(Set.class.isAssignableFrom(rawType) || Collection.class.isAssignableFrom(rawType)){
				return Collections.singleton(createBestExample(parameterizedType.getActualTypeArguments()[0], SetTool
						.concatenate(parents, type)));
			}
			if(Map.class.isAssignableFrom(rawType)){
				Object key = createBestExample(parameterizedType.getActualTypeArguments()[0], SetTool.concatenate(
						parents, type));
				Object value = createBestExample(parameterizedType.getActualTypeArguments()[1], SetTool.concatenate(
						parents, type));
				return Collections.singletonMap(key, value);
			}
			if(Optional.class.isAssignableFrom(rawType)){
				return Optional.of(createBestExample(parameterizedType.getActualTypeArguments()[0], SetTool.concatenate(
						parents, type)));
			}
			if(AutoBuildable.class.isAssignableFrom(rawType)){
				AutoBuildable autoBuildable = ReflectionTool.create(rawType.asSubclass(AutoBuildable.class));
				List<Object> innerObjects = Arrays.stream(parameterizedType.getActualTypeArguments())
						.map(paramType -> createBestExample(paramType, SetTool.concatenate(parents, type)))
						.collect(Collectors.toList());
				return autoBuildable.buildEmpty(innerObjects);
			}
			return createBestExample(rawType, SetTool.concatenate(parents, type));
		}
		// undocumented generic (T or E or PK)
		if(type instanceof TypeVariable){
			return null;
		}
		Class<?> clazz = (Class<?>)type;
		if(clazz.isArray()){
			Object[] array = (Object[])Array.newInstance(clazz.getComponentType(), 1);
			array[0] = createBestExample(clazz.getComponentType(), SetTool.concatenate(parents, type));
			return array;
		}
		if(clazz.isPrimitive()){
			if(type == Boolean.TYPE){ // boolean is the only primitive that doesnt support 0
				return false;
			}
			return 0;
		}
		if(clazz == Boolean.class){
			return false;
		}
		if(clazz == String.class){
			return "";
		}
		if(clazz == Date.class){
			return new Date();
		}
		if(clazz == Integer.class){
			return 0;
		}
		if(clazz == Long.class){
			return 0L;
		}
		if(clazz == Number.class){
			return 0;
		}
		Object example = createWithNulls(clazz);
		for(Field field : ReflectionTool.getDeclaredFieldsIncludingAncestors(clazz)){
			if(clazz.equals(field.getType())){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			field.setAccessible(true);
			try{
				Object fieldExample = createBestExample(field.getGenericType(), SetTool.concatenate(parents, type));
				try{
					field.set(example, fieldExample);
				}catch(Exception e){
					logger.warn("error setting {}", field, e);
				}
			}catch(Exception e){
				logger.warn("error creating {}", type, e);
			}
		}
		return example;
	}

	private static Object createWithNulls(Class<?> type){
		try{
			return UNSAFE_ALLOCATOR.newInstance(type);
		}catch(Exception e){
			throw new RuntimeException("cannot call instanciate " + type, e);
		}
	}

}
