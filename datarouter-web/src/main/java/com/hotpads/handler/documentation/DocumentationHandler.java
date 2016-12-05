package com.hotpads.handler.documentation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.DispatchRule;
import com.hotpads.handler.types.optional.OptionalParameter;

public class DocumentationHandler extends BaseHandler{

	protected List<DocumentedEndpoint> buildDocumentation(BaseDispatcher apiDispatcher, String apiUrlContext){
		return apiDispatcher.getDispatchRules().stream()
				.filter(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.map(this::buildEndpointDocumentation)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<DocumentedEndpoint> buildEndpointDocumentation(DispatchRule rule){
		List<DocumentedEndpoint> endpoints = new ArrayList<>();
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for (Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				String urlSuffix = method.getAnnotation(Handler.class).defaultHandler() ? "" : "/" + method.getName();
				DocumentedEndpoint endpoint = new DocumentedEndpoint();
				endpoint.url = rule.getPattern().pattern().replace(BaseDispatcher.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				endpoint.parameters = new ArrayList<>();
				endpoint.description = method.getAnnotation(Handler.class).description();
				Parameter[] parameters = method.getParameters();
				for(Parameter parameter : parameters){
					DocumentedParameter documentedParameter = new DocumentedParameter();
					endpoint.parameters.add(documentedParameter);
					documentedParameter.name = parameter.getName();
					documentedParameter.type = OptionalParameter.getOptionalInternalType(parameter.getType())
							.getSimpleName();
					documentedParameter.required = !OptionalParameter.class.isAssignableFrom(parameter.getType());
				}
				endpoints.add(endpoint);
			}
			handler = (Class<? extends BaseHandler>) handler.getSuperclass();
		}
		return endpoints;
	}
}