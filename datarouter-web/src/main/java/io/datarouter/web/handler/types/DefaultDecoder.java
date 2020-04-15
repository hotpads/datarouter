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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonSyntaxException;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.optional.OptionalParameter;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DefaultDecoder implements HandlerDecoder{

	//TODO Rename JsonSerializer or add Serializer, we just want a (de)serializer here
	private JsonSerializer deserializer;

	@Inject
	public DefaultDecoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer deserializer){
		this.deserializer = deserializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String, String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		long bodyParamCount = countRequestBodyParam(parameters);
		if(queryParams.size() + bodyParamCount + getOptionalParameterCount(parameters) < parameters.length){
			return null;
		}
		String body = null;
		if(bodyParamCount >= 1){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = new Object[parameters.length];
		for(int i = 0; i < parameters.length; i++){
			Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			Type parameterType = parameter.getType();
			{
				Param parameterAnnotation = parameter.getAnnotation(Param.class);
				if(parameterAnnotation != null){
					if(!parameterAnnotation.value().isEmpty()){
						parameterName = parameterAnnotation.value();
					}
					if(!parameterAnnotation.typeProvider().equals(TypeProvider.class)){
						parameterType = ReflectionTool.create(parameterAnnotation.typeProvider()).get();
					}
				}
			}
			if(parameter.isAnnotationPresent(RequestBody.class)){
				args[i] = decode(body, parameterType);
			}else if(parameter.isAnnotationPresent(RequestBodyString.class)){
				args[i] = body;
			}else{
				String[] queryParam = queryParams.get(parameterName);

				//pre-emptively try to check if the parameter is actually a form-encoded array and normalize the name
				boolean isArray = parameterType instanceof Class && ((Class<?>)parameterType).isArray();
				if(isArray && queryParam == null && !parameterName.endsWith("[]")){
					parameterName += "[]";
					queryParam = queryParams.get(parameterName);
				}

				boolean isOptional = OptionalParameter.class.isAssignableFrom(parameter.getType());
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
						Array.set(typedArray, index, decode(queryParam[index], componentClass));
					}
					args[i] = typedArray;
					continue;
				}

				String parameterValue = queryParam == null ? null : queryParam[0];
				args[i] = isOptional ? OptionalParameter.makeOptionalParameter(parameterValue, parameterType)
						: decode(parameterValue, parameterType);
			}
		}
		return args;
	}

	protected Object decode(String string, Type type){
		try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), "DefaultDecoder deserialize")){
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

	private static long countRequestBodyParam(Parameter[] parameters){
		return Arrays.stream(parameters)
				.filter(parameter -> parameter.isAnnotationPresent(RequestBody.class)
						|| parameter.isAnnotationPresent(RequestBodyString.class))
				.count();
	}

	private static long getOptionalParameterCount(Parameter[] parameters){
		return Arrays.stream(parameters)
				.filter(parameter -> OptionalParameter.class.isAssignableFrom(parameter.getType()))
				.count();
	}

}
