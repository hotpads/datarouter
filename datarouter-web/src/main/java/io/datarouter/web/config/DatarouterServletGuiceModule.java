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
package io.datarouter.web.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import com.google.inject.Provides;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.dispatcher.DefaultDispatcherServlet;
import io.datarouter.web.dispatcher.DispatcherServletClasses;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.dispatcher.FilterParamsSupplier;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.dispatcher.ServletParams;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DefaultAppNavBar;
import io.datarouter.web.user.authenticate.saml.SamlAssertionConsumerServlet;

public class DatarouterServletGuiceModule extends BaseGuiceServletModule{

	private static final String EVERYTHING_BUT_NOT_WEBSOCKET = "(?!/ws/).*";
	private static final String EVERYTHING_BUT_JSP_AND_JSPF = "(?<!jspf|jsp)$";

	private final List<FilterParams> additionalFilterParams;
	private final Class<? extends HttpsConfiguration> httpsConfiguration;
	private final Class<? extends Filter> authenticationFilter;
	private final List<Class<? extends RouteSet>> routeSets;
	private final List<ServletParams> additionalServletParams;
	private final String guicePathsRegex;

	public DatarouterServletGuiceModule(
			List<FilterParams> additionalFilterParams,
			Class<? extends HttpsConfiguration> httpsConfiguration,
			Class<? extends Filter> authenticationFilter,
			List<Class<? extends RouteSet>> routeSets,
			List<ServletParams> additionalServletParams,
			boolean renderJspsUsingServletContainer){
		this.additionalFilterParams = additionalFilterParams;
		this.httpsConfiguration = httpsConfiguration;
		this.authenticationFilter = authenticationFilter;
		this.routeSets = routeSets;
		this.additionalServletParams = additionalServletParams;
		if(renderJspsUsingServletContainer){
			this.guicePathsRegex = EVERYTHING_BUT_NOT_WEBSOCKET + EVERYTHING_BUT_JSP_AND_JSPF;
		}else{
			this.guicePathsRegex = EVERYTHING_BUT_NOT_WEBSOCKET;
		}
	}

	@Override
	protected void configureServlets(){
		//dispatcher and routeSet classes
		bind(DispatcherServletClasses.class)
				.toInstance(new DispatcherServletClasses(List.of(DefaultDispatcherServlet.class)));

		//https configuration implementation
		bind(HttpsConfiguration.class).to(httpsConfiguration);
		rootFilter(authenticationFilter);
		bindActualInstance(FilterParamsSupplier.class, new FilterParamsSupplier(additionalFilterParams));
		additionalFilterParams.forEach(filterParams -> {
			if(filterParams.isRegex){
				filterRegex(filterParams.path).through(filterParams.filterClass);
			}else{
				filter(filterParams.path).through(filterParams.filterClass);
			}
		});

		//additional servlets
		serve(new DatarouterWebPaths().consumer.toSlashedString()).with(SamlAssertionConsumerServlet.class);
		additionalServletParams.forEach(servletParams -> {
			if(servletParams.isRegex){
				serveRegex(servletParams.path).with(servletParams.servletClass);
			}else{
				serve(servletParams.path).with(servletParams.servletClass);
			}
		});

		//rootServlet catch-all comes last
		serveRegex(guicePathsRegex).with(DefaultDispatcherServlet.class);

		//nav bar
		bindActual(AppNavBar.class, DefaultAppNavBar.class);
	}

	@Provides
	public RouteSetRegistry getRouteSetRegistry(DatarouterInjector injector){
		List<RouteSet> routes = new ArrayList<>();
		Scanner.of(routeSets).map(injector::getInstance).flush(routes::addAll);
		return new DefaultRouteSetRegistry(routes);
	}

}
