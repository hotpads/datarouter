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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.handler.mav.nav.NavBar;

@SuppressWarnings("serial")
public abstract class DispatcherServlet extends HttpServlet{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Dispatcher dispatcher;
	@Inject
	private Optional<NavBar> navBar;

	private List<BaseRouteSet> dispatchers = new ArrayList<>();

	@Override
	public void init(){
		registerRouteSets();
		getInitListeners().forEach(listener -> listener.onDispatcherServletInit(this));
	}

	@Override
	public void destroy(){
		getInitListeners().forEach(listener -> listener.onDispatcherServletDestroy(this));
	}

	protected abstract void registerRouteSets();

	protected List<DispatcherServletListener> getInitListeners(){
		List<DispatcherServletListener> initListerners = new ArrayList<>();
		navBar.ifPresent(initListerners::add);
		return initListerners;
	}

	protected final void register(BaseRouteSet routeSet){
		dispatchers.add(routeSet);
	}

	protected final void eagerlyInitRouteSet(BaseRouteSet routeSet){
		// init handlers once, as a sort of eager health check
		Optional.ofNullable(routeSet.getDefaultHandlerClass()).map(injector::getInstance);
		routeSet.getDispatchRules().stream()
				.map(DispatchRule::getHandlerClass)
				.distinct()
				.forEach(injector::getInstance);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException{

		response.setContentType("text/plain");
		response.setHeader("X-Frame-Options", "SAMEORIGIN"); //clickjacking protection

		boolean handled = false;
		for(BaseRouteSet dispatcherRoutes : dispatchers){
			handled = dispatcher.handleRequestIfUrlMatch(request, response, dispatcherRoutes);
			if(handled){
				break;
			}
		}

		if(!handled){
			response.setStatus(404);
			try(PrintWriter out = response.getWriter()){
				out.print(getClass().getCanonicalName() + " could not find Handler for " + request.getRequestURI());
			}
		}
	}

	public DispatchRule findRuleInContext(String path){
		return dispatchers.stream()
				.map(BaseRouteSet::getDispatchRules)
				.flatMap(List::stream)
				.filter(rule -> rule.getPattern().matcher(path).matches())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Path " + path + " has no handler."));
	}

	public List<BaseRouteSet> getRouteSets(){
		return dispatchers;
	}
}
