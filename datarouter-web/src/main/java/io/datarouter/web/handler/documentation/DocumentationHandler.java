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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.handler.types.optional.OptionalParameter;

public class DocumentationHandler extends BaseHandler{

	private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

	@Inject
	private DatarouterWebFiles files;

	private List<DocumentedEndpoint> buildDocumentation(BaseRouteSet routeSet, String apiUrlContext){
		return routeSet.getDispatchRules().stream()
				.filter(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.map(this::buildEndpointDocumentation)
				.flatMap(List::stream)
				.sorted(Comparator.comparing(DocumentedEndpoint::getUrl))
				.collect(Collectors.toList());
	}

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, BaseRouteSet routeSet){
		List<DocumentedEndpoint> endpoints = buildDocumentation(routeSet, apiUrlContext);
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
				DocumentedEndpoint endpoint = new DocumentedEndpoint(url, parameters, description);
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
			methodParameters.add(createDocumentedParameter(parameter.getName(), parameter.getType(),
					parameter.isAnnotationPresent(RequestBody.class)));
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
				.map(parameterName -> createDocumentedParameter(parameterName, String.class, false))
				.collect(Collectors.toList());
	}

	private DocumentedParameter createDocumentedParameter(String parameterName, Class<?> parameterType,
			boolean requestBody){
		DocumentedParameter documentedParameter = new DocumentedParameter();
		documentedParameter.name = parameterName;
		Class<?> type = OptionalParameter.getOptionalInternalType(parameterType);
		documentedParameter.type = type.getSimpleName();
		try{
			documentedParameter.example = gson.toJson(createBestExample(type));
		}catch(Exception e){
			// Expected to not been able to build some object (ex: primitives)
		}
		documentedParameter.required = !OptionalParameter.class.isAssignableFrom(parameterType);
		documentedParameter.requestBody = requestBody;
		return documentedParameter;
	}

	private static Object createBestExample(Class<?> type){
		if(type.isArray()){
			type.getComponentType();
			Object[] array = new Object[1];
			array[0] = createBestExample(type.getComponentType());
			return array;
		}
		Object example = createWithNulls(type);
		for(Field field : type.getDeclaredFields()){
			if(type.equals(field.getType())){
				continue;
			}
			try{
				field.set(example, createBestExample(field.getType()));
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
			int length = constructor.getParameterTypes().length;
			args = Stream.generate(() -> null)
					.limit(length)
					.toArray();
		}
		try{
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		}catch(InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e){
			throw new RuntimeException(e);
		}
	}
}
