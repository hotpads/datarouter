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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.lang.ReflectionTool;
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

	private static final Gson gson = new GsonBuilder()
			.serializeNulls()
			.setPrettyPrinting()
			.create();

	@Inject
	private DatarouterWebFiles files;

	private List<DocumentedEndpoint> buildDocumentation(String apiUrlContext, BaseRouteSet... routeSets){
		return Arrays.stream(routeSets)
				.map(BaseRouteSet::getDispatchRules)
				.flatMap(Collection::stream)
				.filter(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.map(this::buildEndpointDocumentation)
				.flatMap(List::stream)
				.sorted(Comparator.comparing(DocumentedEndpoint::getUrl))
				.collect(Collectors.toList());
	}

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, BaseRouteSet... routeSets){
		List<DocumentedEndpoint> endpoints = buildDocumentation(apiUrlContext, routeSets);
		Mav model = new Mav(files.jsp.docs.dispatcherDocsJsp);
		model.put("endpoints", endpoints);
		model.put("apiName", apiName);
		return model;
	}

	private List<DocumentedEndpoint> buildEndpointDocumentation(DispatchRule rule){
		List<DocumentedEndpoint> endpoints = new ArrayList<>();
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for(Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				String urlSuffix = method.getAnnotation(Handler.class).defaultHandler() ? "" : "/" + method.getName();
				String url = rule.getPattern().pattern().replace(BaseRouteSet.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				List<DocumentedParameter> parameters = new ArrayList<>();
				String description = method.getAnnotation(Handler.class).description();
				parameters.addAll(createApplicableSecurityParameters(rule));
				parameters.addAll(createMethodParameters(method));

				Type genericReturnType = method.getGenericReturnType();
				String response;
				if(genericReturnType == Void.TYPE){
					response = null;
				}else{
					try{
						Object responseExample = createBestExample(genericReturnType, new HashSet<>());
						response = gson.toJson(responseExample);
					}catch(Exception e){
						response = "Impossible to render";
						logger.warn("Could not create response example for {}", genericReturnType, e);
					}
				}
				DocumentedEndpoint endpoint = new DocumentedEndpoint(url, parameters, description, response);
				endpoints.add(endpoint);
			}
			handler = handler.getSuperclass().asSubclass(BaseHandler.class);
		}
		return endpoints;
	}

	private List<DocumentedParameter> createMethodParameters(Method method){
		List<DocumentedParameter> methodParameters = new ArrayList<>();
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

	private List<DocumentedParameter> createApplicableSecurityParameters(DispatchRule rule){
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

	private DocumentedParameter createDocumentedParameter(String parameterName, Type parameterType,
			boolean requestBody, String description){
		DocumentedParameter documentedParameter = new DocumentedParameter();
		documentedParameter.name = parameterName;
		Type type = OptionalParameter.getOptionalInternalType(parameterType);
		documentedParameter.type = type instanceof Class ? ((Class<?>)type).getSimpleName() : type.toString();
		try{
			documentedParameter.example = gson.toJson(createBestExample(type, new HashSet<>()));
		}catch(Exception e){
			logger.warn("Could not create parameter example for {}", type, e);
		}
		documentedParameter.required = !(parameterType instanceof Class) || !OptionalParameter.class.isAssignableFrom(
				(Class<?>)parameterType);
		documentedParameter.requestBody = requestBody;
		documentedParameter.description = description;
		return documentedParameter;
	}

	private static Object createBestExample(Type type, Set<Type> parents){
		if(parents.contains(type)){
			return null;
		}
		if(type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Class<?> rawType = (Class<?>)parameterizedType.getRawType();
			if(Collection.class.isAssignableFrom(rawType)){
				return Arrays.asList(createBestExample(parameterizedType.getActualTypeArguments()[0], SetTool
						.concatenate(parents, type)));
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
		Class<?> clazz = (Class<?>)type;
		if(clazz.isArray()){
			Object[] array = new Object[1];
			array[0] = createBestExample(clazz.getComponentType(), SetTool.concatenate(parents, type));
			return array;
		}
		Object example = createWithNulls(clazz);
		for(Field field : clazz.getDeclaredFields()){
			if(clazz.equals(field.getType())){
				continue;
			}
			try{
				field.set(example, createBestExample(field.getType(), SetTool.concatenate(parents, type)));
			}catch(Exception e){
				// Excepted to not been able to set some field (ex inside String)
			}
		}
		return example;
	}

	private static Object createWithNulls(Class<?> type){
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		Optional<Constructor<?>> noArgConstructor = Arrays.stream(constructors)
				.filter(constructor -> constructor.getParameterTypes().length == 0)
				.findAny();
		Constructor<?> constructor;
		Object[] args;
		if(noArgConstructor.isPresent()){
			constructor = noArgConstructor.get();
			args = new Object[0];
		}else{
			constructor = constructors[0];
			args = Arrays.stream(constructor.getParameterTypes())
					.map(argType -> {
						if(!argType.isPrimitive()){
							return null;
						}
						if(argType == Boolean.TYPE){ // boolean is the only primitive that doesnt support 0
							return false;
						}
						return 0;
					})
					.toArray();
		}
		try{
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		}catch(InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e){
			throw new RuntimeException("cannot call " + constructor, e);
		}
	}

}
