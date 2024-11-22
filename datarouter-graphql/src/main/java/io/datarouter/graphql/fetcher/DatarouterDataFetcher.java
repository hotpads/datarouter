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
package io.datarouter.graphql.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.service.GraphQlSchemaService.EmptyGraphQlArgumentType;
import io.datarouter.graphql.tool.GraphQlTool;
import io.datarouter.graphql.util.GraphQlCounters;
import io.datarouter.graphql.util.TypedGraphQlContext;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.gson.DatarouterGsons;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.web.handler.validator.RequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;
import jakarta.inject.Inject;

public abstract class DatarouterDataFetcher<
		T,
		R extends GraphQlArgumentType>
implements DataFetcher<T>{

	@Inject
	private DatarouterInjector injector;

	public DataFetchingEnvironment environment;
	public TypedGraphQlContext context;
	public R args;
	private HttpServletRequest request;
	protected List<RequestParamValidator<?>> argValidators = new ArrayList<>();

	public abstract T build();
	public abstract T buildInvalidArgResponse(RequestParamValidatorResponseDto validationStatus);
	public abstract boolean trace();

	@Override
	public T get(DataFetchingEnvironment environment){
		if(!trace()){
			return buildResponse(environment);
		}
		try(var $ = TracerTool.startSpanNoGroupType(this.getClass().getSimpleName())){
			return buildResponse(environment);
		}
	}

	private T buildResponse(DataFetchingEnvironment environment){
		long start = System.currentTimeMillis();
		GraphQlCounters.incDataFetcher(this);
		assignVariables(environment);
		RequestParamValidatorResponseDto validationStatus = buildAndValidateArgs();
		if(!validationStatus.success()){
			return buildInvalidArgResponse(validationStatus);
		}
		T result = build();
		long end = System.currentTimeMillis();
		Metrics.measure(GraphQlCounters.DATA_FETCHER + " " + this.getClass().getSimpleName() + " durationMs", end
				- start);
		return result;
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	protected RequestParamValidatorResponseDto buildAndValidateArgs(){
		Class<R> classOfR = (Class<R>)GraphQlTool.getArgumentClassFromFetcherClass(this.getClass());
		if(!EmptyGraphQlArgumentType.class.isAssignableFrom(classOfR)){
			JsonElement element = DatarouterGsons.withUnregisteredEnums().toJsonTree(environment.getArguments());
			args = DatarouterGsons.withUnregisteredEnums().fromJson(element, classOfR);
			return validateArgs();
		}
		return RequestParamValidatorResponseDto.makeSuccessResponse();
	}

	protected void assignVariables(DataFetchingEnvironment environment){
		this.environment = environment;
		this.context = new TypedGraphQlContext(environment.getContext());
	}

	protected RequestParamValidatorResponseDto validateArgs(){
		Map<Class<? extends RequestParamValidator<?>>,Object> validatorMap = argumentsToValidators();
		if(validatorMap.isEmpty()){
			return RequestParamValidatorResponseDto.makeSuccessResponse();
		}
		for(Map.Entry<Class<? extends RequestParamValidator<?>>,Object> pair : validatorMap.entrySet()){
			@SuppressWarnings("unchecked")
			RequestParamValidator<T> validator = (RequestParamValidator<T>)injector.getInstance(pair.getKey());
			argValidators.add(validator);
			RequestParamValidatorResponseDto response = validator.validate(getRequest(), validator.getParameterClass()
					.cast(pair.getValue()));
			if(!response.success()){
				return response;
			}
		}
		return RequestParamValidatorResponseDto.makeSuccessResponse();
	}

	protected Map<Class<? extends RequestParamValidator<?>>,Object> argumentsToValidators(){
		return Map.of();
	}

	protected <P,V extends RequestParamValidator<P>> V getArgValidator(Class<V> cls, P argValue){
		RequestParamValidator<?> paramValidator = argValidators.stream()
				.filter(validator -> validator.getClass().equals(cls))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("argValidator unavailable: " + cls.getSimpleName()
						+ ", argValue: " + argValue));
		return cls.cast(paramValidator);
	}

	protected HttpServletRequest getRequest(){
		if(request != null){
			return request;
		}
		return context.find(GraphQlBaseHandler.HTTP_REQUEST)
				.orElseThrow(() -> new RuntimeException("Missing HttpServletRequest"));
	}

}
