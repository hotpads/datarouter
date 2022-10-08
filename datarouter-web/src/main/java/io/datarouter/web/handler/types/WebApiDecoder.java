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
package io.datarouter.web.handler.types;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.endpoint.web.BaseWebApi;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.json.JsonSerializer;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;
import io.datarouter.web.util.http.RequestTool;

// copied from EndpointDecoder - need to remove the duplicate code
@Singleton
public class WebApiDecoder implements HandlerDecoder, JsonAwareHandlerCodec{
	private static final Logger logger = LoggerFactory.getLogger(WebApiDecoder.class);

	private final JsonSerializer deserializer;

	@Inject
	public WebApiDecoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer deserializer){
		this.deserializer = deserializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> webApiType = parameters[0].getType();
		if(!EndpointTool.paramIsWebApiObject(method)){
			String message = String.format("object needs to extend BaseWebApi for %s.%s",
					method.getDeclaringClass().getSimpleName(),
					method.getName());
			throw new RuntimeException(message);
		}

		// populate the fields with baseEndpoint with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseWebApi<?,?> baseWebApi = ReflectionTool.createWithoutNoArgs((Class<? extends BaseWebApi<?,?>>)webApiType);

		if(!baseWebApi.method.matches(request.getMethod())){
			logger.error("Request type mismatch. Handler={} WebApi={}",
					baseWebApi.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findRequestBody(baseWebApi.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromWebApiObject(queryParams, baseWebApi, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] getArgsFromWebApiObject(
			Map<String,String[]> queryParams,
			BaseWebApi<?,?> baseWebApi,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseWebApi.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = EndpointTool.getFieldName(field);
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(body, field.getGenericType());
				field.set(baseWebApi, requestBody);
				if(requestBody instanceof Collection<?> requestBodyCollection){
					// Datarouter handler method batch <Handler.class.simpleName> <methodName>
					String counter = String.format("Datarouter handler method batch %s %s",
							method.getDeclaringClass().getSimpleName(),
							method.getName());
					Counters.inc(counter, requestBodyCollection.size());
				}
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
				field.set(baseWebApi, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseWebApi, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseWebApi, Optional.empty());
				}else{
					Type type = EndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterValue, type);
					field.set(baseWebApi, Optional.of(optionalValue));
				}
			}else{
				field.set(baseWebApi, decodeType(parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseWebApi;
		return args;
	}

	// same as DefaultDecoder.decode (keeping duplicate code for now)
	private Object decodeType(String string, Type type){
		try(var $ = TracerTool.startSpan(getClass().getSimpleName() + " deserialize",
				TraceSpanGroupType.SERIALIZATION)){
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

	@Override
	public JsonSerializer getJsonSerializer(){
		return deserializer;
	}

}
