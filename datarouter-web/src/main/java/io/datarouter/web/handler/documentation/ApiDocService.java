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
import java.lang.reflect.WildcardType;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.datarouter.httpclient.DocumentedGenericHolder;
import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.EndpointParam;
import io.datarouter.httpclient.endpoint.EndpointRequestBody;
import io.datarouter.httpclient.endpoint.EndpointTool;
import io.datarouter.httpclient.endpoint.IgnoredField;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.serialization.CompatibleDateTypeAdapter;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.handler.types.optional.OptionalParameter;

@Singleton
public class ApiDocService{
	private static final Logger logger = LoggerFactory.getLogger(ApiDocService.class);

	private static final Set<String> HIDDEN_SPEC_PARAMS = Set.of(
			SecurityParameters.CSRF_IV,
			SecurityParameters.CSRF_TOKEN,
			SecurityParameters.SIGNATURE);

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Date.class, new CompatibleDateTypeAdapter())
			.serializeNulls()
			.setPrettyPrinting()
			.create();

	public List<DocumentedEndpointJspDto> buildDocumentation(String apiUrlContext, List<BaseRouteSet> routeSets){
		return Scanner.of(routeSets)
				.concatIter(BaseRouteSet::getDispatchRules)
				.include(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.concatIter(this::buildEndpointDocumentation)
				.sorted(Comparator.comparing(DocumentedEndpointJspDto::getUrl))
				.list();
	}

	private List<DocumentedEndpointJspDto> buildEndpointDocumentation(DispatchRule rule){
		List<DocumentedEndpointJspDto> endpoints = new ArrayList<>();
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for(Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				Handler handlerAnnotation = method.getAnnotation(Handler.class);
				String url = rule.getPattern().pattern();
				if(!url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)){ // not a handleDir
					if(!url.endsWith(method.getName()) && !handlerAnnotation.defaultHandler()){
						continue;
					}
				}
				if(url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)){
					String urlSuffix = handlerAnnotation.defaultHandler() ? "" : "/" + method.getName();
					url = url.replace(BaseRouteSet.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				}
				String implementation = handler.getSimpleName();
				List<DocumentedParameterJspDto> parameters = new ArrayList<>();
				String description = handlerAnnotation.description();
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
				String responseTypeString = buildTypeString(responseType);
				var response = new DocumentedResponseJspDto(responseTypeString, responseExample);
				var endpoint = new DocumentedEndpointJspDto(url, implementation, parameters, description, response);
				endpoints.add(endpoint);
			}
			handler = handler.getSuperclass().asSubclass(BaseHandler.class);
		}
		return endpoints;
	}

	private static String buildTypeString(Type type){
		if(type instanceof Class){
			return ((Class<?>)type).getSimpleName();
		}else if(type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType)type;
			String responseTypeString = ((Class<?>)parameterizedType.getRawType()).getSimpleName();
			String paramterizedType = Scanner.of(parameterizedType.getActualTypeArguments())
					.map(ApiDocService::buildTypeString)
					.collect(Collectors.joining(",", "<", ">"));
			return responseTypeString + paramterizedType;
		}else{
			return type.toString();
		}
	}

	private List<DocumentedParameterJspDto> createMethodParameters(Method method){
		Parameter[] parameters = method.getParameters();
		boolean isEndpointObject = EndpointTool.paramIsEndpointObject(method);
		if(isEndpointObject){
			@SuppressWarnings("unchecked")
			BaseEndpoint<?> baseEndpoint = ReflectionTool.createWithoutNoArgs(
					(Class<? extends BaseEndpoint<?>>)method.getParameters()[0].getType());
			return Scanner.of(baseEndpoint.getClass().getFields())
					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
					.map(this::createDocumentedParameterFromField)
					.list();
		}
		return Scanner.of(parameters)
				.map(this::createDocumentedParameterJspDto)
				.list();
	}

	private DocumentedParameterJspDto createDocumentedParameterJspDto(Parameter parameter){
		String description = null;
		String name = parameter.getName();
		Param param = parameter.getAnnotation(Param.class);
		if(param != null){
			description = param.description();
			if(!param.value().isEmpty()){
				name = param.value();
			}
		}
		return createDocumentedParameter(
				name,
				parameter.getParameterizedType(),
				parameter.isAnnotationPresent(RequestBody.class),
				description);
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
		Type type = OptionalParameter.getOptionalInternalType(parameterType);
		Optional<Class<?>> clazz = type instanceof Class ? Optional.of((Class<?>)type) : Optional.empty();
		String example = null;
		if(includeType(clazz)){
			try{
				example = GSON.toJson(createBestExample(type, new HashSet<>()));
			}catch(Exception e){
				logger.warn("Could not create parameter example {} for {}", type, parameterName, e);
			}
		}
		boolean isRequired = !(parameterType instanceof Class)
				|| !OptionalParameter.class.isAssignableFrom((Class<?>)parameterType);

		return new DocumentedParameterJspDto(
				parameterName,
				clazz.map(Class::getSimpleName).orElse(type.toString()),
				example,
				isRequired,
				requestBody,
				HIDDEN_SPEC_PARAMS.contains(parameterName),
				description);
	}

	private DocumentedParameterJspDto createDocumentedParameterFromField(Field field){
		boolean isOptional = field.getType().isAssignableFrom(Optional.class);
		String type;
		String example = null;
		if(isOptional){
			Class<?> parameterizedType = (Class<?>)((ParameterizedType)field.getGenericType())
					.getActualTypeArguments()[0];
			type = parameterizedType.getSimpleName();
			if(includeType(Optional.of(parameterizedType))){
				try{
					example = GSON.toJson(createBestExample(parameterizedType, new HashSet<>()));
				}catch(Exception e){
					logger.warn("Could not create parameter example {} for {}", field.getType(), field.getName(), e);
				}
			}
		}else{
			type = field.getType().getSimpleName();
			if(includeType(Optional.of(field.getType()))){
				try{
					example = GSON.toJson(createBestExample(field.getType(), new HashSet<>()));
				}catch(Exception e){
					logger.warn("Could not create parameter example {} for {}", field.getType(), field.getName(), e);
				}
			}
		}
		return new DocumentedParameterJspDto(
				EndpointTool.getFieldName(field),
				type,
				example,
				!isOptional,
				field.isAnnotationPresent(EndpointRequestBody.class),
				HIDDEN_SPEC_PARAMS.contains(field.getName()),
				Optional.ofNullable(field.getAnnotation(EndpointParam.class))
						.map(EndpointParam::description)
						.orElse(null));
	}

	private boolean includeType(Optional<Class<?>> type){
		return !type.map(cls -> Number.class.isAssignableFrom(cls)).orElse(false)
				&& !type.map(cls -> String.class.isAssignableFrom(cls)).orElse(false)
				&& !type.map(cls -> Boolean.class.isAssignableFrom(cls)).orElse(false)
				&& !type.map(cls -> cls.isPrimitive()).orElse(false);
	}

	private static Object createBestExample(Type type, Set<Type> parents){
		return createBestExample(type, parents, 0);
	}

	private static Object createBestExample(Type type, Set<Type> parents, int callWithoutWarning){
		if(parents.contains(type)){
			return null;
		}
		callWithoutWarning = callWithoutWarning - 1;
		Set<Type> parentsWithType = Scanner.of(parents)
				.append(type)
				.collect(HashSet::new);
		if(type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Class<?> rawType = (Class<?>)parameterizedType.getRawType();
			Type type0 = parameterizedType.getActualTypeArguments()[0];
			if(List.class.isAssignableFrom(rawType)){
				return List.of(createBestExample(type0, parentsWithType));
			}
			if(Set.class.isAssignableFrom(rawType) || Collection.class.isAssignableFrom(rawType)){
				return Collections.singleton(createBestExample(type0, parentsWithType));
			}
			if(Map.class.isAssignableFrom(rawType)){
				Object key = createBestExample(type0, parentsWithType);
				Object value = createBestExample(parameterizedType.getActualTypeArguments()[1], parentsWithType);
				return Collections.singletonMap(key, value);
			}
			if(Optional.class.isAssignableFrom(rawType)){
				return Optional.of(createBestExample(type0, parentsWithType));
			}
			if(DocumentedGenericHolder.class.isAssignableFrom(rawType)){
				DocumentedGenericHolder autoBuildable = (DocumentedGenericHolder)createBestExample(rawType, parents, 3);
				List<Object> innerObjects = Scanner.of(parameterizedType.getActualTypeArguments())
						.map(paramType -> createBestExample(paramType, parentsWithType))
						.list();
				List<String> fieldNames = autoBuildable.getGenericFieldNames();
				for(int i = 0; i < innerObjects.size(); i++){
					String fieldName = fieldNames.get(i);
					Field field;
					try{
						field = rawType.getDeclaredField(fieldName);
					}catch(NoSuchFieldException | SecurityException e){
						logger.warn("fieldName={} rawType={}", fieldName, rawType, e);
						continue;
					}
					field.setAccessible(true);
					Object value = innerObjects.get(i);
					ReflectionTool.set(field, autoBuildable, value);
				}
				return autoBuildable;
			}
			return createBestExample(rawType, parentsWithType);
		}
		// undocumented generic (T or E or PK)
		if(type instanceof TypeVariable){
			if(callWithoutWarning < 1){
				logger.warn("undocumneted generic, please use AutoBuildable type={} parents={}", type, parents);
			}
			return null;
		}
		if(type instanceof WildcardType){
			logger.warn("please document type={} parents={}", type, parents);
			return null;
		}
		Class<?> clazz = (Class<?>)type;
		if(clazz.isArray()){
			if(clazz.getComponentType().isPrimitive()){
				Object array = Array.newInstance(clazz.getComponentType(), 1);
				return array;
			}else{
				Object[] array = (Object[])Array.newInstance(clazz.getComponentType(), 1);
				array[0] = createBestExample(clazz.getComponentType(), parentsWithType);
				return array;
			}
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
		if(clazz == char.class || clazz == Character.class){
			return 'c';
		}
		if(clazz == Date.class){
			return new Date();
		}
		if(clazz == Integer.class
				|| clazz == Long.class
				|| clazz == Number.class){
			return 0;
		}
		if(clazz == LocalDateTime.class){
			return LocalDateTime.now();
		}
		if(clazz == JsonArray.class){
			return new JsonArray();
		}
		if(clazz == JsonObject.class){
			return new JsonObject();
		}
		Object example = ReflectionTool.createNullArgsWithUnsafeAllocator(clazz);
		for(Field field : ReflectionTool.getDeclaredFieldsIncludingAncestors(clazz)){
			if(clazz.equals(field.getType())){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			field.setAccessible(true);
			try{
				Object fieldExample = createBestExample(field.getGenericType(), parentsWithType, callWithoutWarning);
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

}
