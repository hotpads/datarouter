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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.ParamType;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.json.JsonSerializer;

public class JavaEndpointTool{
	private static final Logger logger = LoggerFactory.getLogger(JavaEndpointTool.class);

	public static DatarouterHttpRequest toDatarouterHttpRequest(BaseJavaEndpoint<?,?> endpoint,
			JsonSerializer serializer){
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
		ParamsMap paramsMap = JavaEndpointTool.getParamFields(endpoint, serializer);
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
	 * <p>
	 * EndpointRequestBodies are ignored.
	 * Both GET and POST params will be added to the map
	 * <p>
	 * If optional fields are not present, they will not be added to the map
	 */
	public static ParamsMap getParamFields(BaseJavaEndpoint<?,?> endpoint, JsonSerializer serializer){
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

	public static Optional<Object> findEntity(BaseJavaEndpoint<?,?> endpoint){
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

	@Deprecated
	public static Type getResponseType(BaseJavaEndpoint<?,?> endpoint){
		return getResponseType(endpoint.getClass());
	}

	public static Type getResponseType(Class<?> clazz){
		return ((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public static Type extractParameterizedType(Field field){
		return ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
	}

}
