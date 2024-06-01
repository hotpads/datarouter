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
package io.datarouter.httpclient.endpoint.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.FormData;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.ParamType;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.endpoint.web.BaseWebApi;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.json.JsonSerializer;

public class EndpointTool{
	private static final Logger logger = LoggerFactory.getLogger(EndpointTool.class);

	public static DatarouterHttpRequest toDatarouterHttpRequest(BaseEndpoint<?,?> endpoint, JsonSerializer serializer){
		Objects.requireNonNull(endpoint.urlPrefix);
		String finalUrl = URI.create(endpoint.urlPrefix + endpoint.pathNode.toSlashedString()).normalize().toString();
		DatarouterHttpRequest request = new DatarouterHttpRequest(
				endpoint.method,
				finalUrl,
				endpoint.shouldSkipSecurity,
				endpoint.shouldSkipLogs);
		request.setRetrySafe(endpoint.getRetrySafe());
		endpoint.timeout.ifPresent(request::setTimeout);
		endpoint.headers.forEach((key,values) -> values.forEach(value -> request.addHeader(key, value)));
		endpoint.cookies.forEach(request::addCookie);
		ParamsMap paramsMap = EndpointTool.getParamFields(endpoint, serializer);
		request.addGetParams(paramsMap.getParams);
		request.addPostParams(paramsMap.postParams);
		return request;
	}

	public static class ParamsMap{

		public final Map<String,String> getParams;
		public final Map<String,String> postParams;

		public ParamsMap(Map<String,String> getParams, Map<String,String> postParams){
			this.getParams = getParams;
			this.postParams = postParams;
		}

	}

	public static class ParamsKeysMap{

		public final List<String> getKeys;
		public final List<String> postKeys;

		public ParamsKeysMap(List<String> getKeys, List<String> postKeys){
			this.getKeys = getKeys;
			this.postKeys = postKeys;
		}

		public List<String> getAllKeys(){
			List<String> keys = new ArrayList<>();
			keys.addAll(getKeys);
			keys.addAll(postKeys);
			return keys;
		}

	}

	/**
	 * Convert all field names and values to a map.
	 *
	 * EndpointRequestBodies are ignored.
	 * Both GET and POST params will be added to the map
	 *
	 * If optional fields are not present, they will not be added to the map
	 */
	public static ParamsMap getParamFields(BaseEndpoint<?,?> endpoint, JsonSerializer serializer){
		Map<String,String> getParams = new LinkedHashMap<>();
		Map<String,String> postParams = new LinkedHashMap<>();
		for(Field field : endpoint.getClass().getFields()){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			if(field.getAnnotation(RequestBody.class) != null){
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
			Optional<String> parsedValue = getValue(field, value, serializer);
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				if(endpoint.method == HttpRequestMethod.GET){
					parsedValue.ifPresent(paramValue -> getParams.put(key, paramValue));
				}else if(endpoint.method == HttpRequestMethod.POST){
					parsedValue.ifPresent(paramValue -> postParams.put(key, paramValue));
				}
				continue;
			}
			ParamType paramType = param.paramType();
			if(paramType == ParamType.GET){
				parsedValue.ifPresent(paramValue -> getParams.put(key, paramValue));
			}else if(paramType == ParamType.POST){
				parsedValue.ifPresent(paramValue -> postParams.put(key, paramValue));
			}
		}
		return new ParamsMap(getParams, postParams);
	}

	private static ParamsKeysMap getRequiredKeys(Field[] fields, HttpRequestMethod method){
		List<String> getKeys = new LinkedList<>();
		List<String> postKeys = new LinkedList<>();
		for(Field field : fields){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			if(field.getAnnotation(RequestBody.class) != null){
				continue;
			}
			boolean isOptional = field.getType().isAssignableFrom(Optional.class);
			if(isOptional){
				continue;
			}

			String key = getFieldName(field);

			EndpointParam param = field.getAnnotation(EndpointParam.class);
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				if(method == HttpRequestMethod.GET){
					getKeys.add(key);
				}else if(method == HttpRequestMethod.POST){
					postKeys.add(key);
				}
				continue;
			}
			ParamType paramType = param.paramType();
			if(paramType == ParamType.GET){
				getKeys.add(key);
			}else if(paramType == ParamType.POST){
				postKeys.add(key);
			}
		}
		return new ParamsKeysMap(getKeys, postKeys);
	}

	public static ParamsKeysMap getRequiredKeys(BaseEndpoint<?,?> endpoint){
		return getRequiredKeys(endpoint.getClass().getFields(), endpoint.method);
	}

	public static ParamsKeysMap getRequiredKeys(BaseWebApi<?,?> webApi){
		return getRequiredKeys(webApi.getClass().getFields(), webApi.method);
	}

	public static Optional<Object> findEntity(BaseEndpoint<?,?> endpoint){
		for(Field field : endpoint.getClass().getFields()){
			if(field.isAnnotationPresent(RequestBody.class)){
				try{
					return Optional.of(field.get(endpoint));
				}catch(IllegalArgumentException | IllegalAccessException ex){
					logger.error("", ex);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<Object> findEntity(BaseWebApi<?,?> webApi){
		for(Field field : webApi.getClass().getFields()){
			if(field.isAnnotationPresent(RequestBody.class)){
				try{
					return Optional.of(field.get(webApi));
				}catch(IllegalArgumentException | IllegalAccessException ex){
					logger.error("", ex);
				}
			}
		}
		return Optional.empty();
	}

	private static boolean hasEntity(Class<?> clazz){
		for(Field field : clazz.getFields()){
			if(field.isAnnotationPresent(RequestBody.class)){
				return true;
			}
		}
		return false;
	}


	public static Optional<String> getValue(Field field, Object value, JsonSerializer serializer){
		if(!field.getType().isAssignableFrom(Optional.class)){
			String newValue;
			Class<?> type = field.getType();
			if(type.isAssignableFrom(String.class)){
				newValue = value.toString();
			}else{
				newValue = serializer.serialize(value);
			}
			return Optional.of(newValue);
		}

		Optional<?> optionalValue = (Optional<?>)value;
		if(optionalValue.isPresent()){
			String newValue;
			Type parameterizedType = extractParameterizedType(field);
			if(parameterizedType.getTypeName().equals(String.class.getTypeName())){
				newValue = optionalValue.get().toString();
			}else{
				newValue = serializer.serialize(optionalValue.get());
			}
			return Optional.of(newValue);
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

	public static Optional<Field> findFormData(Field[] fields){
		return Stream.of(fields)
				.filter(field -> field.isAnnotationPresent(FormData.class))
				.findFirst();
	}

	public static Optional<Field> findRequestBody(Field[] fields){
		return Stream.of(fields)
				.filter(field -> field.isAnnotationPresent(RequestBody.class))
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

	public static boolean paramIsWebApiObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return method.getParameterCount() == 1 && BaseWebApi.class.isAssignableFrom(endpointType);
	}

	public static boolean paramIsLinkObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return method.getParameterCount() == 1 && BaseLink.class.isAssignableFrom(endpointType);
	}

	@Deprecated
	public static Type getResponseType(BaseEndpoint<?,?> endpoint){
		return getResponseType(endpoint.getClass());
	}

	public static Type getResponseType(Class<?> clazz){
		return ((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public static Type extractParameterizedType(Field field){
		return ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
	}

	public static void validateEndpoint(BaseEndpoint<?,?> endpoint){
		HttpRequestMethod requestType = endpoint.method;
		if(requestType == HttpRequestMethod.GET){
			if(hasEntity(endpoint.getClass())){
				String message = endpoint.getClass().getSimpleName() + " - GET request cannot have a POST-body";
				throw new IllegalArgumentException(message);
			}
		}
		// This is not a hard protocol restriction.
		// Datarouter has trouble decoding if there is a body and post param for a POST request.
		if(requestType == HttpRequestMethod.POST){
			if(hasEntity(endpoint.getClass()) && containsParamOfType(endpoint, ParamType.POST)){
				String message = endpoint.getClass().getSimpleName()
						+ " - Request cannot have a POST-body and POST params";
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
		}
	}

	public static void validateWebApi(BaseWebApi<?,?> webApi){
		HttpRequestMethod requestType = webApi.method;
		if(requestType == HttpRequestMethod.GET){
			if(hasEntity(webApi.getClass())){
				String message = webApi.getClass().getSimpleName() + " - GET request cannot have a POST-body";
				throw new IllegalArgumentException(message);
			}
		}
		// This is not a hard protocol restriction.
		// Datarouter has trouble decoding if there is a body and post param for a POST request.
		if(requestType == HttpRequestMethod.POST){
			if(hasEntity(webApi.getClass()) && containsParamOfType(webApi.getClass(), ParamType.POST)){
				String message = webApi.getClass().getSimpleName()
						+ " - Request cannot have a POST-body and POST params";
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
		}
	}

	@Deprecated
	private static boolean containsParamOfType(BaseEndpoint<?,?> endpoint, ParamType type){
		return containsParamOfType(endpoint.getClass(), type);
	}

	private static boolean containsParamOfType(Class<?> clazz, ParamType type){
		for(Field field : clazz.getFields()){
			if(field.getAnnotation(IgnoredField.class) != null){
				continue;
			}
			if(field.getAnnotation(RequestBody.class) != null){
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
