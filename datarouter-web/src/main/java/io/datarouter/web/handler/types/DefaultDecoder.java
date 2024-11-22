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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.JavaEndpointTool;
import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.endpoint.param.FormData;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.json.JsonSerializer;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.MethodParameterExtractionTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.api.external.BaseExternalEndpoint;
import io.datarouter.web.api.mobile.BaseMobileEndpoint;
import io.datarouter.web.api.web.BaseWebApi;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.optional.OptionalParameter;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

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
		if(EndpointTool.paramIsJavaEndpointObject(method)){
			return decodeEndpoint(request, method);
		}
		if(EndpointTool.paramIsWebApiObject(method)){
			return decodeWebApi(request, method);
		}
		if(EndpointTool.paramIsMobileEndpointObject(method)){
			return decodeMobileApi(request, method);
		}
		if(EndpointTool.paramIsExternalEndpointObject(method)){
			return decodeExternalApi(request, method);
		}
		if(EndpointTool.paramIsLinkObject(method)){
			return decodeLink(request, method);
		}
		return decodeDefault(request, method);
	}

	@SuppressWarnings("deprecation")
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
				if(parameterAnnotation != null && !parameterAnnotation.value().isEmpty()){
					parameterName = parameterAnnotation.value();
				}
			}
			if(parameter.isAnnotationPresent(RequestBody.class)){
				args[i] = decodeType(parameterName, body, parameterType);
			}else if(parameter.isAnnotationPresent(RequestBodyString.class)){
				args[i] = body;
			}else{
				String[] queryParam = queryParams.get(parameterName);

				//pre-emptively try to check if the parameter is actually a form-encoded array and normalize the name
				boolean isArray = parameterType instanceof Class cls && cls.isArray();
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
						Array.set(typedArray, index, decodeType(parameterName, queryParam[index], componentClass));
					}
					args[i] = typedArray;
					continue;
				}

				String parameterValue = queryParam == null ? null : queryParam[0];
				if(isOptional){
					if(OptionalParameter.class.isAssignableFrom(parameter.getType())){
						args[i] = OptionalParameter.makeOptionalParameter(parameterValue, parameterType, method,
								parameter);
					}else{
						Class<?> innerType = MethodParameterExtractionTool
								.extractParameterizedTypeFromOptionalParameter(parameter);
						if(parameterValue == null || parameterValue.isEmpty()){
							args[i] = Optional.empty();
						}else{
							Object value = decodeType(parameterName, parameterValue, innerType);
							args[i] = Optional.ofNullable(value);
						}
					}
				}else{
					args[i] = decodeType(parameterName, parameterValue, parameterType);
				}
			}
		}
		return args;
	}

	private Object[] decodeEndpoint(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		if(!EndpointTool.paramIsJavaEndpointObject(method)){
			String message = String.format("object needs to extend BaseJavaEndpoint for %s.%s",
					method.getDeclaringClass().getSimpleName(),
					method.getName());
			throw new RuntimeException(message);
		}

		// populate the fields with baseEndpoint with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseJavaEndpoint<?,?> baseJavaEndpoint = ReflectionTool.createWithoutNoArgs(
				(Class<? extends BaseJavaEndpoint<?,?>>)endpointType);

		if(!baseJavaEndpoint.method.matches(request.getMethod())){
			logger.error("Request type mismatch. RequestURI={}, Handler={} Endpoint={}",
					request.getRequestURI(),
					baseJavaEndpoint.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findRequestBody(baseJavaEndpoint.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromEndpointObject(queryParams, baseJavaEndpoint, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	// TODO remove duplicate code between this method and getArgsFromWebApiObject(
	private Object[] getArgsFromEndpointObject(
			Map<String,String[]> queryParams,
			BaseJavaEndpoint<?,?> baseJavaEndpoint,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseJavaEndpoint.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = JavaEndpointTool.getFieldName(field);
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(parameterName, body, field.getGenericType());
				field.set(baseJavaEndpoint, requestBody);
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
					Array.set(typedArray, index, decodeType(parameterName, queryParam[index], componentClass));
				}
				field.set(baseJavaEndpoint, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseJavaEndpoint, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseJavaEndpoint, Optional.empty());
				}else{
					Type type = JavaEndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterName, parameterValue, type);
					field.set(baseJavaEndpoint, Optional.of(optionalValue));
				}
			}else{
				field.set(baseJavaEndpoint, decodeType(parameterName, parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseJavaEndpoint;
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
			logger.error("Request type mismatch. RequestURI={}, Handler={} WebApi={}",
					request.getRequestURI(),
					baseWebApi.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findFormData(baseWebApi.getClass().getFields()).isEmpty()
				&& EndpointTool.findRequestBody(baseWebApi.getClass().getFields()).isPresent()){
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

	private Object[] decodeMobileApi(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> mobileApiType = parameters[0].getType();
		if(!EndpointTool.paramIsMobileEndpointObject(method)){
			String message = String.format("object needs to extend BaseMobileApi for %s.%s",
					method.getDeclaringClass().getSimpleName(),
					method.getName());
			throw new RuntimeException(message);
		}

		// populate the fields with baseEndpoint with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseMobileEndpoint<?,?> baseMobileEndpoint = ReflectionTool
				.createWithoutNoArgs((Class<? extends BaseMobileEndpoint<?,?>>)mobileApiType);

		if(!baseMobileEndpoint.method.matches(request.getMethod())){
			logger.error("Request type mismatch. RequestURI={}, Handler={} MobileApi={}",
					request.getRequestURI(),
					baseMobileEndpoint.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findFormData(baseMobileEndpoint.getClass().getFields()).isEmpty()
				&& EndpointTool.findRequestBody(baseMobileEndpoint.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromMobileApiObject(queryParams, baseMobileEndpoint, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] decodeExternalApi(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> externalApiType = parameters[0].getType();
		if(!EndpointTool.paramIsExternalEndpointObject(method)){
			String message = String.format("object needs to extend BaseExternalApi for %s.%s",
					method.getDeclaringClass().getSimpleName(),
					method.getName());
			throw new RuntimeException(message);
		}

		// populate the fields with baseEndpoint with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseExternalEndpoint<?,?> baseExternalEndpoint = ReflectionTool
				.createWithoutNoArgs((Class<? extends BaseExternalEndpoint<?,?>>)externalApiType);

		if(!baseExternalEndpoint.method.matches(request.getMethod())){
			logger.error("Request type mismatch. RequestURI={}, Handler={} ExternalApi={}",
					request.getRequestURI(),
					baseExternalEndpoint.method.persistentString,
					request.getMethod());
		}

		String body = null;
		if(EndpointTool.findRequestBody(baseExternalEndpoint.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromExternalApiObject(queryParams, baseExternalEndpoint, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] getArgsFromExternalApiObject(
			Map<String,String[]> queryParams,
			BaseExternalEndpoint<?,?> baseExternalEndpoint,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseExternalEndpoint.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = JavaEndpointTool.getFieldName(field);
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(parameterName, body, field.getGenericType());
				field.set(baseExternalEndpoint, requestBody);
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
					Array.set(typedArray, index, decodeType(parameterName, queryParam[index], componentClass));
				}
				field.set(baseExternalEndpoint, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseExternalEndpoint, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseExternalEndpoint, Optional.empty());
				}else{
					Type type = JavaEndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterName, parameterValue, type);
					field.set(baseExternalEndpoint, Optional.of(optionalValue));
				}
			}else{
				field.set(baseExternalEndpoint, decodeType(parameterName, parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseExternalEndpoint;
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

			String parameterName = JavaEndpointTool.getFieldName(field);
			Type parameterType = field.getGenericType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(FormData.class)){
				continue;
			}

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(parameterName, body, field.getGenericType());
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
			boolean isArray;
			boolean isList;
			boolean isIterable;
			if(parameterType instanceof Class<?> parameterClass){
				isArray = parameterClass.isArray();
				isList = false;
				isIterable = isArray;
			}else if(parameterType instanceof ParameterizedType parameterParameterizedType){
				isArray = false;
				isList = List.class.isAssignableFrom((Class<?>)parameterParameterizedType.getRawType());
				isIterable = isList;
			}else{
				isArray = false;
				isList = false;
				isIterable = false;
			}
			if(isIterable && queryParam == null && !parameterName.endsWith("[]")){
				parameterName += "[]";
				queryParam = queryParams.get(parameterName);
			}

			if(queryParam == null && !isOptional){
				return null;
			}

			boolean isFormEncodedArray = queryParam != null
					&& (queryParam.length > 1 || parameterName.endsWith("[]"))
					&& isIterable;

			if(isFormEncodedArray){
				Object iterable;
				if(isArray){
					Class<?> componentClass = ((Class<?>)parameterType).getComponentType();
					iterable = Array.newInstance(componentClass, queryParam.length);
					for(int index = 0; index < queryParam.length; index++){
						Array.set(iterable, index, decodeType(parameterName, queryParam[index], componentClass));
					}
				}else if(isList){
					List<Object> list = new ArrayList<>(queryParam.length);
					for(String queryParamValue : queryParam){
						list.add(decodeType(
								parameterName,
								queryParamValue,
								((ParameterizedType)parameterType).getActualTypeArguments()[0]));
					}
					iterable = list;
				}else{
					throw new RuntimeException("unrecognized iterable");
				}
				field.set(baseWebApi, iterable);
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
					Type type = JavaEndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterName, parameterValue, type);
					field.set(baseWebApi, Optional.of(optionalValue));
				}
			}else{
				field.set(baseWebApi, decodeType(parameterName, parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseWebApi;
		return args;
	}

	// TODO - https://zillowgroup.atlassian.net/browse/IN-12210
	// getArgsFromWebApiObject, getArgsFromMobileApiObject and getArgsFromEndpointObject are
	// all copies of one another. Extract this logic out into common code.
	// all similar to one another. It will be a bit tricky but we should extract this logic out
	// into common code where possible.
	private Object[] getArgsFromMobileApiObject(
			Map<String,String[]> queryParams,
			BaseMobileEndpoint<?,?> baseMobileEndpoint,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseMobileEndpoint.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = JavaEndpointTool.getFieldName(field);
			Type parameterType = field.getGenericType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(FormData.class)){
				continue;
			}

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(parameterName, body, field.getGenericType());
				field.set(baseMobileEndpoint, requestBody);
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
			boolean isArray;
			boolean isList;
			boolean isIterable;
			if(parameterType instanceof Class<?> parameterClass){
				isArray = parameterClass.isArray();
				isList = false;
				isIterable = isArray;
			}else if(parameterType instanceof ParameterizedType parameterParameterizedType){
				isArray = false;
				isList = List.class.isAssignableFrom((Class<?>)parameterParameterizedType.getRawType());
				isIterable = isList;
			}else{
				isArray = false;
				isList = false;
				isIterable = false;
			}
			if(isIterable && queryParam == null && !parameterName.endsWith("[]")){
				parameterName += "[]";
				queryParam = queryParams.get(parameterName);
			}

			if(queryParam == null && !isOptional){
				return null;
			}

			boolean isFormEncodedArray = queryParam != null
					&& (queryParam.length > 1 || parameterName.endsWith("[]"))
					&& isIterable;

			if(isFormEncodedArray){
				Object iterable;
				if(isArray){
					Class<?> componentClass = ((Class<?>)parameterType).getComponentType();
					iterable = Array.newInstance(componentClass, queryParam.length);
					for(int index = 0; index < queryParam.length; index++){
						Array.set(iterable, index, decodeType(parameterName, queryParam[index], componentClass));
					}
				}else if(isList){
					List<Object> list = new ArrayList<>(queryParam.length);
					for(String queryParamValue : queryParam){
						list.add(decodeType(
								parameterName,
								queryParamValue,
								((ParameterizedType)parameterType).getActualTypeArguments()[0]));
					}
					iterable = list;
				}else{
					throw new RuntimeException("unrecognized iterable");
				}
				field.set(baseMobileEndpoint, iterable);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseMobileEndpoint, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseMobileEndpoint, Optional.empty());
				}else{
					Type type = JavaEndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterName, parameterValue, type);
					field.set(baseMobileEndpoint, Optional.of(optionalValue));
				}
			}else{
				field.set(baseMobileEndpoint, decodeType(parameterName, parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseMobileEndpoint;
		return args;
	}

	public Object[] decodeLink(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> linkType = parameters[0].getType();
		if(!LinkTool.paramIsLinkObject(method)){
			throw new RuntimeException("object needs to extend BaseLink");
		}

		// populate the fields with baseLink with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseLink<?> baseLink = ReflectionTool.createWithoutNoArgs((Class<? extends BaseLink<?>>)linkType);

		String body = null;
		if(EndpointTool.findRequestBody(baseLink.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromLinkObject(queryParams, baseLink, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] getArgsFromLinkObject(
			Map<String,String[]> queryParams,
			BaseLink<?> baseLink,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseLink.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = JavaEndpointTool.getFieldName(field);
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(parameterName, body, field.getGenericType());
				field.set(baseLink, requestBody);
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
					Array.set(typedArray, index, decodeType(parameterName, queryParam[index], componentClass));
				}
				field.set(baseLink, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseLink, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseLink, Optional.empty());
				}else{
					Type type = JavaEndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterName, parameterValue, type);
					field.set(baseLink, Optional.of(optionalValue));
				}
			}else{
				field.set(baseLink, decodeType(parameterName, parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseLink;
		return args;
	}

	private static long countRequestBodyParam(Parameter[] parameters){
		return Scanner.of(parameters)
				.include(parameter -> parameter.isAnnotationPresent(RequestBody.class)
						|| parameter.isAnnotationPresent(RequestBodyString.class))
				.count();
	}

	@SuppressWarnings("deprecation")
	private static long getOptionalParameterCount(Parameter[] parameters){
		return Scanner.of(parameters)
				.map(Parameter::getType)
				.include(type -> OptionalParameter.class.isAssignableFrom(type)
						|| Optional.class.isAssignableFrom(type))
				.count();
	}

}
