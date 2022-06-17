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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DatarouterNavBar;

@SuppressWarnings("serial")
public abstract class DispatcherServlet extends HttpServlet{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Dispatcher dispatcher;
	@Inject
	private Optional<AppNavBar> appNavBar;
	@Inject
	private DatarouterNavBar datarouterNavBar;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;

	private List<RouteSet> routeSets = new ArrayList<>();

	@Override
	public void init(){
		registerRouteSets();
		ensureUniqueDispatchRules();
		getInitListeners().forEach(listener -> listener.onDispatcherServletInit(this));
	}

	@Override
	public void destroy(){
		getInitListeners().forEach(listener -> listener.onDispatcherServletDestroy(this));
	}

	protected abstract void registerRouteSets();

	protected List<DispatcherServletListener> getInitListeners(){
		List<DispatcherServletListener> initListerners = new ArrayList<>();
		appNavBar.ifPresent(initListerners::add);
		initListerners.add(datarouterNavBar);
		return initListerners;
	}

	protected final void register(RouteSet newRouteSet){
		routeSets.add(newRouteSet);
	}

	protected final void eagerlyInitRouteSet(BaseRouteSet routeSet){
		// init handlers once, as a sort of eager health check
		Optional.ofNullable(routeSet.getDefaultHandlerClass())
				.map(injector::getInstance);
		routeSet.getDispatchRules().stream()
				.map(DispatchRule::getHandlerClass)
				.distinct()
				.forEach(injector::getInstance);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException{

		response.setHeader("X-Frame-Options", "SAMEORIGIN"); //clickjacking protection
		long keepAliveTimeoutS = datarouterWebSettingRoot.keepAliveTimeout.get().toSecond();
		if(keepAliveTimeoutS > 0){
			response.setHeader("Keep-Alive", "timeout=" + keepAliveTimeoutS);
		}

		RoutingResult routingResult = RoutingResult.NOT_FOUND;
		for(RouteSet dispatcherRoutes : routeSets){
			routingResult = dispatcher.handleRequestIfUrlMatch(request, response, dispatcherRoutes);
			if(routingResult != RoutingResult.NOT_FOUND){
				break;
			}
		}

		switch(routingResult){
		case NOT_FOUND:
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType("text/plain");
			try(PrintWriter out = response.getWriter()){
				out.print(getClass().getCanonicalName() + " could not find Handler for " + request.getRequestURI());
			}
			break;
		case FORBIDDEN:
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			break;
		default:
			break;
		}
	}

	public Optional<DispatchRule> findRuleInContext(String path){
		return routeSets.stream()
				.map(RouteSet::getDispatchRules)
				.flatMap(List::stream)
				.filter(rule -> rule.getPattern().matcher(path).matches())
				.findFirst();
	}

	public List<RouteSet> getRouteSets(){
		return routeSets;
	}

	private void ensureUniqueDispatchRules(){
		routeSets.stream()
				.map(RouteSet::getDispatchRules)
				.flatMap(List::stream)
				.map(rule -> new Pair<>(rule.getRegex(), rule.getHandlerClass()))
				.reduce(new HashSet<>(), (rules, rule) -> {
					if(rules.contains(rule)){
						throw new IllegalStateException("Duplicate DispatchRule " + rule);
					}
					rules.add(rule);
					return rules;
				}, (rulesA, rulesB) -> {
					rulesA.addAll(rulesB);
					return rulesA;
				});
	}

}