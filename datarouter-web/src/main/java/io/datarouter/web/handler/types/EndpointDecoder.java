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
package io.datarouter.web.handler.types;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.EndpointEntity;
import io.datarouter.httpclient.endpoint.IgnoredField;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class EndpointDecoder implements HandlerDecoder{
	private static final Logger logger = LoggerFactory.getLogger(EndpointDecoder.class);

	@Inject
	@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)
	private JsonSerializer deserializer;

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		boolean isEndpointObject = parameters.length == 1
				&& parameters[0].getType().getClass().isInstance(BaseEndpoint.class);
		if(!isEndpointObject){
			throw new RuntimeException("object needs to extend BaseEndpoint");
		}
		BaseEndpoint<?> baseEndpoint = (BaseEndpoint<?>)ReflectionTool.create(parameters[0].getType());
		if(baseEndpoint == null || baseEndpoint.getClass() == null || baseEndpoint.getClass().getFields() == null){
			throw new RuntimeException("BaseEndpoint is null, a no-arg constructor might be missing");
		}
		String body = null;
		if(getEntity(baseEndpoint.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromEndpointObject(queryParams, baseEndpoint, body);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] getArgsFromEndpointObject(Map<String,String[]> queryParams, BaseEndpoint<?> baseEndpoint,
			String body)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseEndpoint.getClass().getFields();
		for(Field field : fields){
			IgnoredField param = field.getAnnotation(IgnoredField.class);
			if(param != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = field.getName();
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.getAnnotation(EndpointEntity.class) != null){
				var entity = decodeType(body, parameterType);
				field.set(baseEndpoint, entity);
				continue;
			}

			boolean isOptional = field.getType().isAssignableFrom(Optional.class);

			// pre-emptively try to check if the parameter is actually a form-encoded array and normalize the name
			boolean isArray = parameterType instanceof Class && ((Class<?>)parameterType).isArray();
			if(isArray && queryParam == null && !parameterName.endsWith("[]")){
				parameterName += "[]";
				queryParam = queryParams.get(parameterName);
			}

			if(queryParam == null && !isOptional){
				return null;
			}

			boolean isFormEncodedArray = queryParam != null
					&& (queryParam.length > 1 || parameterName.endsWith("[]"))
					&& isArray;

			if(isFormEncodedArray){
				Class<?> componentClass = ((Class<?>)parameterType).getComponentType();
				Object typedArray = Array.newInstance(componentClass, queryParam.length);
				for(int index = 0; index < queryParam.length; index++){
					Array.set(typedArray, index, decodeType(queryParam[index], componentClass));
				}
				field.set(baseEndpoint, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseEndpoint, Optional.empty());
				}else{
					// maybe hacky
					Class<?> clazz = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
					var optionalValue = decodeType(parameterValue, clazz);
					field.set(baseEndpoint, Optional.of(optionalValue));
				}
			}else{
				field.set(baseEndpoint, decodeType(parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseEndpoint;
		return args;
	}

	// same as DefaultDecoder.decode (keeping duplicate code for now)
	private Object decodeType(String string, Type type){
		try(var $ = TracerTool.startSpan("DefaultDecoder deserialize")){
			TracerTool.appendToSpanInfo("characters", string.length());
			// this prevents empty strings from being decoded as null by gson
			Object obj;
			try{
				obj = deserializer.deserialize(string, type);
			}catch(JsonSyntaxException e){
				// If the JSON is malformed and String is expected, just assign the string
				if(type.equals(String.class)){
					return string;
				}
				throw new RuntimeException("failed to decode " + string + " to a " + type, e);
			}
			// deserialized successfully as null, but we want empty string instead of null for consistency with Params
			if(obj == null && type.equals(String.class) && !"null".equals(string)){
				return "";
			}
			if(obj == null){
				throw new RuntimeException("could not decode " + string + " to a non null " + type);
			}
			return obj;
		}
	}

	private static Optional<Field> getEntity(Field[] fields){
		return Scanner.of(fields)
				.include(field -> field.getAnnotation(EndpointEntity.class) != null)
				.findFirst();
	}

}
