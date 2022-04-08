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
package io.datarouter.httpclient.endpoint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;

public class EndpointTool{
	private static final Logger logger = LoggerFactory.getLogger(EndpointTool.class);

	public static DatarouterHttpRequest toDatarouterHttpRequest(BaseEndpoint<?,?> endpoint){
		Objects.requireNonNull(endpoint.urlPrefix);
		String finalUrl = URI.create(endpoint.urlPrefix + endpoint.pathNode.toSlashedString()).normalize().toString();
		DatarouterHttpRequest request = new DatarouterHttpRequest(
				endpoint.method,
				finalUrl,
				endpoint.shouldSkipSecurity,
				endpoint.shouldSkipLogs);
		request.setRetrySafe(endpoint.retrySafe);
		endpoint.timeout.ifPresent(request::setTimeout);
		endpoint.headers.forEach((key,values) -> {
			values.forEach(value -> request.addHeader(key, value));
		});
		for(Field field : endpoint.getClass().getFields()){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			String key = getFieldName(field);
			Object value = null;
			try{
				value = field.get(endpoint);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			boolean isOptional = field.getType().isAssignableFrom(Optional.class);
			if(isOptional && value == null){
				throw new RuntimeException(String.format(
						"%s: Optional fields cannot be null. '%s' needs to be initialized to Optional.empty().",
						endpoint.getClass().getSimpleName(), key));
			}
			if(value == null){
				throw new RuntimeException(String.format(
						"%s: Fields cannot be null. '%s' needs to be initialized or changed to an Optional field.",
						endpoint.getClass().getSimpleName(), key));
			}
			EndpointParam param = field.getAnnotation(EndpointParam.class);
			Optional<String> parsedValue = getValue(field, value);
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				parsedValue.ifPresent(paramValue -> request.addParam(key, paramValue));
				continue;
			}
			ParamType paramType = param.paramType();
			if(paramType == ParamType.GET){
				parsedValue.ifPresent(paramValue -> request.addGetParam(key, paramValue));
			}else if(paramType == ParamType.POST){
				parsedValue.ifPresent(paramValue -> request.addPostParam(key, paramValue));
			}
		}
		return request;
	}

	/**
	 * Convert all field names and values to a map.
	 *
	 * EndpointRequestBodies are ignored.
	 * Both GET and POST params will be added to the map
	 * If optional fields are not present, they will not be added to the map
	 */
	public static Map<String,String> getParamFields(BaseEndpoint<?,?> endpoint){
		Map<String,String> params = new LinkedHashMap<>();
		for(Field field : endpoint.getClass().getFields()){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			if(field.getAnnotation(EndpointRequestBody.class) != null){
				continue;
			}
			String key = getFieldName(field);
			Object value = null;
			try{
				value = field.get(endpoint);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			boolean isOptional = field.getType().isAssignableFrom(Optional.class);
			if(isOptional && value == null){
				throw new RuntimeException(String.format(
						"%s: Optional fields cannot be null. '%s' needs to be initialized to Optional.empty().",
						endpoint.getClass().getSimpleName(), key));
			}
			if(value == null){
				throw new RuntimeException(String.format(
						"%s: Fields cannot be null. '%s' needs to be initialized or changed to an Optional field.",
						endpoint.getClass().getSimpleName(), key));
			}
			getValue(field, value).ifPresent(paramValue -> params.put(key, paramValue));
		}
		return params;
	}

	public static Optional<Object> findEntity(BaseEndpoint<?,?> endpoint){
		for(Field field : endpoint.getClass().getFields()){
			if(field.isAnnotationPresent(EndpointRequestBody.class)){
				try{
					return Optional.of(field.get(endpoint));
				}catch(IllegalArgumentException | IllegalAccessException ex){
					logger.error("", ex);
				}
			}
		}
		return Optional.empty();
	}

	private static boolean hasEntity(BaseEndpoint<?,?> endpoint){
		for(Field field : endpoint.getClass().getFields()){
			if(field.isAnnotationPresent(EndpointRequestBody.class)){
				return true;
			}
		}
		return false;
	}

	public static Optional<String> getValue(Field field, Object value){
		if(!field.getType().isAssignableFrom(Optional.class)){
			return Optional.of(value.toString());
		}
		Optional<?> optionalValue = (Optional<?>)value;
		if(optionalValue.isPresent()){
			return Optional.of(optionalValue.get().toString());
		}
		return Optional.empty();
	}

	public static String getFieldName(Field field){
		EndpointParam endpointParam = field.getAnnotation(EndpointParam.class);
		return Optional.ofNullable(endpointParam)
				.map(EndpointParam::serializedName)
				.filter(name -> !name.isEmpty())
				.orElseGet(field::getName);
	}

	public static Optional<Field> findRequestBody(Field[] fields){
		return Stream.of(fields)
				.filter(field -> field.isAnnotationPresent(EndpointRequestBody.class))
				.findFirst();
	}

	public static boolean paramIsEndpointObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return method.getParameterCount() == 1 && BaseEndpoint.class.isAssignableFrom(endpointType);
	}

	public static Type getResponseType(BaseEndpoint<?,?> endpoint){
		return ((ParameterizedType)endpoint.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public static Type extractParameterizedType(Field field){
		return ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
	}

	public static void validateEndpoint(BaseEndpoint<?,?> endpoint){
		HttpRequestMethod requestType = endpoint.method;
		if(requestType == HttpRequestMethod.GET){
			if(findEntity(endpoint).isPresent()){
				String message = endpoint.getClass().getSimpleName() + " - GET request cannot have a POST-body";
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
		}
		// This is not a hard protocol restriction.
		// Datarouter has trouble decoding if there is a body and post param for a POST request.
		if(requestType == HttpRequestMethod.POST){
			if(hasEntity(endpoint) && containsParamOfType(endpoint, ParamType.POST)){
				String message = endpoint.getClass().getSimpleName()
						+ " - Request cannot have a POST-body and POST params";
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
		}
	}

	private static boolean containsParamOfType(BaseEndpoint<?,?> endpoint, ParamType type){
		for(Field field : endpoint.getClass().getFields()){
			if(field.getAnnotation(IgnoredField.class) != null){
				continue;
			}
			if(field.getAnnotation(EndpointRequestBody.class) != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			EndpointParam param = field.getAnnotation(EndpointParam.class);
			ParamType paramType;
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				paramType = type;
			}else{
				paramType = param.paramType();
			}
			if(paramType == ParamType.GET && type == ParamType.GET){
				return true;
			}
			if(paramType == ParamType.POST && type == ParamType.POST){
				return true;
			}
		}
		return false;
	}

}
