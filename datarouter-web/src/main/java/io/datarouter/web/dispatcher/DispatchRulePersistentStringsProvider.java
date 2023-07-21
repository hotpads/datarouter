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
package io.datarouter.web.dispatcher;

import java.util.List;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DispatchRulePersistentStringsProvider{

	@Inject
	private DatarouterInjector injector;

	public List<String> getAvailableEndpoints(Class<? extends ApiKeyPredicate> predicateClass){
		return injector.scanValuesOfType(DispatcherServlet.class)
				.concatIter(DispatcherServlet::getRouteSets)
				.concat(routeSet -> getApplicableEndpoints(routeSet, predicateClass))
				.distinct()
				.list();
	}

	private static Scanner<String> getApplicableEndpoints(RouteSet routeSet,
			Class<? extends ApiKeyPredicate> predicateClass){
		return Scanner.of(routeSet.getDispatchRules())
				.include(DispatchRule::hasApiKey)
				.include(rule -> predicateClass.isAssignableFrom(rule.getApiKeyPredicate().getClass()))
				.map(DispatchRule::getPersistentString)
				.concat(OptionalScanner::of);
	}

}
