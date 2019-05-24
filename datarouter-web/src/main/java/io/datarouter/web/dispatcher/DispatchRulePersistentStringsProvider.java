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
package io.datarouter.web.dispatcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;

@Singleton
public class DispatchRulePersistentStringsProvider{

	@Inject
	private DatarouterInjector injector;

	public List<String> getAvailableEndpoints(Class<? extends ApiKeyPredicate> predicateClass){
		return injector.getInstancesOfType(DispatcherServlet.class).values().stream()
				.map(DispatcherServlet::getRouteSets)
				.flatMap(List::stream)
				.flatMap(routeSet -> getApplicableEndpoints(routeSet, predicateClass))
				.collect(Collectors.toList());
	}

	private static Stream<String> getApplicableEndpoints(BaseRouteSet routeSet,
			Class<? extends ApiKeyPredicate> predicateClass){
		return routeSet.getDispatchRules().stream()
				.filter(DispatchRule::hasApiKey)
				.filter(rule -> predicateClass.isAssignableFrom(rule.getApiKeyPredicate().getClass()))
				.map(DispatchRule::getPersistentString)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

}
