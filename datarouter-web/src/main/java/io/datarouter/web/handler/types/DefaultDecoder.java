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

import io.datarouter.httpclient.endpoint.java.BaseEndpoint;
import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.endpoint.web.BaseWebApi;
import io.datarouter.json.JsonSerializer;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.MethodParameterExtractionTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.optional.OptionalParameter;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DefaultDecoder implements JsonAwareHandlerDecoder{
	private static final Logger logger = LoggerFactory.getLogger(DefaultDecoder.class);

	private final JsonSerializer serializer;

	@Inject
	public DefaultDecoder(
			@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)
			JsonSerializer serializer){
		this.serializer = serializer;
	}

	@Override
	public JsonSerializer getJsonSerializer(){
		return serializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		if(EndpointTool.paramIsEndpointObject(method)){
			return decodeEndpoint(request, method);
		}
		if(EndpointTool.paramIsWebApiObject(method)){
			return decodeWebApi(request, method);
		}
		return decodeDefault(request, method);
	}

	private Object[] decodeDefault(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
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
			Type parameterType = parameter.getParameterizedType();
			{
				Param parameterAnnotation = parameter.getAnnotation(Param.class);
				if(parameterAnnotation != null){
					if(!parameterAnnotation.value().isEmpty()){
						parameterName = parameterAnnotation.value();
					}
				}
			}
			if(parameter.isAnnotationPresent(RequestBody.class)){
				args[i] = decodeType(body, parameterType);
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

				boolean isOptional = OptionalParameter.class.isAssignableFrom(parameter.getType())
						|| Optional.class.isAssignableFrom(parameter.getType());
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
					args[i] = typedArray;
					continue;
				}

				String parameterValue = queryParam == null ? null : queryParam[0];
				if(isOptional){
					if(OptionalParameter.class.isAssignableFrom(parameter.getType())){
						args[i] = OptionalParameter.makeOptionalParameter(parameterValue, parameterType);
					}else{
						Class<?> innerType = MethodParameterExtractionTool
								.extractParameterizedTypeFromOptionalParameter(parameter);
						if(parameterValue == null || parameterValue.isEmpty()){
							args[i] = Optional.empty();
						}else{
							Object value = decodeType(parameterValue, innerType);
							args[i] = Optional.ofNullable(value);
						}
					}
				}else{
					args[i] = decodeType(parameterValue, parameterType);
				}
			}
		}
		return args;
	}

	private Object[] decodeEndpoint(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		if(!EndpointTool.paramIsEndpointObject(method)){
			String message = String.format("object needs to extend BaseEndpoint for %s.%s",
					method.getDeclaringClass().getSimpleName(),
					method.getName());
			throw new RuntimeException(message);
		}

		// populate the fields with baseEndpoint with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseEndpoint<?,?> baseEndpoint = ReflectionTool.createWithoutNoArgs(
				(Class<? extends BaseEndpoint<?,?>>)endpointType);

		if(!baseEndpoint.method.matches(request.getMethod())){
			logger.error("Request type mismatch. Handler={} Endpoint={}",
					baseEndpoint.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findRequestBody(baseEndpoint.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromEndpointObject(queryParams, baseEndpoint, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	// TODO remove duplicate code between this method and getArgsFromWebApiObject(
	private Object[] getArgsFromEndpointObject(
			Map<String,String[]> queryParams,
			BaseEndpoint<?,?> baseEndpoint,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseEndpoint.getClass().getFields();
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
				field.set(baseEndpoint, requestBody);
				if(requestBody instanceof Collection<?> requestBodyCollection){
					// Datarouter handler method batch <Handler.class.simpleName> <methodName>
					@SuppressWarnings("unchecked")
					Class<? extends BaseHandler> handlerClass = (Class<? extends BaseHandler>)method
							.getDeclaringClass();
					HandlerMetrics.incRequestBodyCollectionSize(
							handlerClass,
							method,
							requestBodyCollection.size());
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
				field.set(baseEndpoint, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseEndpoint, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseEndpoint, Optional.empty());
				}else{
					Type type = EndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterValue, type);
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

	private Object[] decodeWebApi(HttpServletRequest request, Method method){
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
					@SuppressWarnings("unchecked")
					Class<? extends BaseHandler> handlerClass = (Class<? extends BaseHandler>)method
							.getDeclaringClass();
					HandlerMetrics.incRequestBodyCollectionSize(
							handlerClass,
							method,
							requestBodyCollection.size());
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

	private static long countRequestBodyParam(Parameter[] parameters){
		return Scanner.of(parameters)
				.include(parameter -> parameter.isAnnotationPresent(RequestBody.class)
						|| parameter.isAnnotationPresent(RequestBodyString.class))
				.count();
	}

	private static long getOptionalParameterCount(Parameter[] parameters){
		return Scanner.of(parameters)
				.map(Parameter::getType)
				.include(type -> OptionalParameter.class.isAssignableFrom(type)
						|| Optional.class.isAssignableFrom(type))
				.count();
	}

}
