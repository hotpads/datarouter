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
package io.datarouter.metric.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.BaseHandler.Handler.HandlerUsageType;
import io.datarouter.web.handlerusage.HandlerUsageBuilder;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageQueryItemRequestDto;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageQueryItemResponseDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HandlerUsageService{

	@Inject
	private RouteSetRegistry routeSetRegistry;
	@Inject
	private HandlerUsageBuilder handlerUsageBuilder;
	@Inject
	private ServiceName serviceName;

	public List<HandlerUsageQueryItemResponseDto> getAllHandlerUsageMetrics(
			DatarouterDuration window,
			String username,
			Threads threads){
		return handlerUsageBuilder.getHandlerUsage(
				generateQueryItemDtos(),
				serviceName.get(),
				window,
				username,
				threads);
	}

	public Map<String,String> getRouteSetNamesByClass(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.toMap(rule -> rule.getHandlerClass().getSimpleName(),
						rule -> rule.getRouteSet().getClass().getSimpleName());
	}

	public ActionableHandlers getActionableHandlers(Long daysToQuery){
		List<HandlerUsageQueryItemResponseDto> handlerUsageMetrics = getAllHandlerUsageMetrics(
				new DatarouterDuration(daysToQuery, TimeUnit.DAYS),
				getClass().getSimpleName(),
				Threads.none());

		List<HandlerMethodNameAndClassName> unusedHandlerMethods = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() == 0)
				.exclude(metricDto -> handlerIsMarkedAsOneOf(metricDto, List.of(
						HandlerUsageType.INFREQUENTLY_USED,
						HandlerUsageType.TEMPORARILY_UNUSED,
						HandlerUsageType.NON_PROD_ONLY)))
				.map(metricDto -> new HandlerMethodNameAndClassName(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		List<HandlerMethodNameAndClassName> usedButMarkedAsTemporarilyUnused = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() > 0)
				.include(metricDto -> handlerIsMarkedAsOneOf(metricDto, List.of(HandlerUsageType.TEMPORARILY_UNUSED)))
				.map(metricDto -> new HandlerMethodNameAndClassName(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		List<HandlerMethodNameAndClassName> usedButMarkedAsNonProdOnly = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() > 0)
				.include(metricDto -> handlerIsMarkedAsOneOf(metricDto, List.of(HandlerUsageType.NON_PROD_ONLY)))
				.map(metricDto -> new HandlerMethodNameAndClassName(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();
		return new ActionableHandlers(
				unusedHandlerMethods,
				usedButMarkedAsTemporarilyUnused,
				usedButMarkedAsNonProdOnly);
	}

	public record HandlerMethodNameAndClassName(
			String methodName,
			String className){
	}

	public record ActionableHandlers(
			List<HandlerMethodNameAndClassName> unusedHandlerMethods,
			List<HandlerMethodNameAndClassName> usedButMarkedAsTemporarilyUnused,
			List<HandlerMethodNameAndClassName> usedButMarkedAsNonProdOnly){
	}

	private List<HandlerUsageQueryItemRequestDto> generateQueryItemDtos(){
		List<HandlerUsageQueryItemRequestDto> handlerQueryDtos = new ArrayList<>();
		Scanner.of(getHandlerMethodsByClass().entrySet())
				.forEach(entry -> Scanner.of(entry.getValue())
						.forEach(handlerMethod ->
								handlerQueryDtos.add(buildUsageQueryItemDto(
										handlerMethod,
										entry.getKey()))));
		return handlerQueryDtos;
	}

	private Map<String,List<Method>> getHandlerMethodsByClass(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.map(DispatchRule::getHandlerClass)
				.distinct()
				.toMap(
						Class::getSimpleName,
						clazz -> Scanner.of(clazz.getDeclaredMethods())
								.include(method ->
										method.getAnnotation(Handler.class) != null
												&& !method.getAnnotation(Handler.class).defaultHandler()
												&& method.getAnnotation(Handler.class).deprecatedOn().isEmpty())
								.list());
	}

	private static boolean handlerIsMarkedAsOneOf(
			HandlerUsageQueryItemResponseDto metricDto,
			List<HandlerUsageType> usageTypes){
		return usageTypes.contains(metricDto.usageType());
	}

	private HandlerUsageQueryItemRequestDto buildUsageQueryItemDto(Method handlerMethod, String className){
		return new HandlerUsageQueryItemRequestDto(
				className,
				handlerMethod.getName(),
				handlerMethod.getAnnotation(Handler.class).usageType(),
				UUID.randomUUID().toString());
	}
}
