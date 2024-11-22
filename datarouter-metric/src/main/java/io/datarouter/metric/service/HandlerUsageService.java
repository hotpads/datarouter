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

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handlerusage.HandlerUsageBuilder;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageDto;
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

	public List<HandlerUsageDto> getHandlerUsageMetrics(
			DatarouterDuration window,
			String username){
		return handlerUsageBuilder
				.getHandlerUsage(
						extractMetricNames(),
						serviceName.get(),
						window,
						username);
	}

	private List<String> extractMetricNames(){
		List<String> exactMetricNames = new ArrayList<>();
		Scanner.of(getHandlerMethodsByClass().entrySet())
				.forEach(entry -> Scanner.of(entry.getValue())
						.map(Method::getName)
						.forEach(methodName ->
								exactMetricNames.add(buildMethodMetricName(entry.getKey(), methodName))));
		return exactMetricNames;
	}

	private Map<String,List<Method>> getHandlerMethodsByClass(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.map(DispatchRule::getHandlerClass)
				.toMap(
						Class::getSimpleName,
						clazz -> Scanner.of(clazz.getDeclaredMethods())
								.include(method ->
										method.getAnnotation(Handler.class) != null
												&& !method.getAnnotation(Handler.class).defaultHandler())
								.list());
	}

	public Map<String,String> getRouteSetNamesByClass(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.toMap(rule -> rule.getHandlerClass().getSimpleName(),
						rule -> rule.getRouteSet().getClass().getSimpleName());
	}

	private static String buildMethodMetricName(String className, String methodName){
		return "Datarouter handler method " + className + " " + methodName;
	}
}
