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
package io.datarouter.web.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;

import com.google.inject.Provides;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatcherServlet;
import io.datarouter.web.dispatcher.DispatcherServletClasses;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.dispatcher.ServletParams;
import io.datarouter.web.filter.StaticFileFilter;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DefaultAppNavBar;
import io.datarouter.web.user.authenticate.saml.SamlAssertionConsumerServlet;

public class DatarouterServletGuiceModule extends BaseGuiceServletModule{

	private static final String EVERYTHING_BUT_NOT_WEBSOCKET = "(?!/ws/).*";
	private static final String EVERYTHING_BUT_JSP_AND_JSPF = "(?<!jspf|jsp)$";

	private final List<FilterParams> additionalFilterParams;
	private final boolean excludeStaticFileFilter;
	private final Class<? extends HttpsConfiguration> httpsConfigurationClass;
	private final Class<? extends Filter> authenticationFilterClass;
	private final Class<? extends DispatcherServlet> rootDispatcherServletClass;
	private final List<Class<? extends BaseRouteSet>> additionalRootRouteSetClasses;
	private final List<BaseRouteSet> rootRouteSets;
	private final List<ServletParams> additionalServletParams;
	private final String guicePathsRegex;
	private final FilterParams healthcheckFilterParams;

	public DatarouterServletGuiceModule(
			List<FilterParams> additionalFilterParams,
			boolean excludeStaticFileFilter,
			Class<? extends HttpsConfiguration> httpsConfigurationClass,
			Class<? extends Filter> authenticationFilterClass,
			Class<? extends DispatcherServlet> rootDispatcherServletClass,
			List<Class<? extends BaseRouteSet>> additionalRootRouteSetClasses,
			List<BaseRouteSet> additionalRootRouteSets,
			List<ServletParams> additionalServletParams,
			boolean renderJspsUsingServletContainer,
			FilterParams healthcheckFilterParams){
		this.additionalFilterParams = additionalFilterParams;
		this.excludeStaticFileFilter = excludeStaticFileFilter;
		this.httpsConfigurationClass = httpsConfigurationClass;
		this.authenticationFilterClass = authenticationFilterClass;
		this.rootDispatcherServletClass = rootDispatcherServletClass;
		this.additionalRootRouteSetClasses = additionalRootRouteSetClasses;
		this.rootRouteSets = additionalRootRouteSets;
		this.additionalServletParams = additionalServletParams;
		if(renderJspsUsingServletContainer){
			this.guicePathsRegex = EVERYTHING_BUT_NOT_WEBSOCKET + EVERYTHING_BUT_JSP_AND_JSPF;
		}else{
			this.guicePathsRegex = EVERYTHING_BUT_NOT_WEBSOCKET;
		}
		this.healthcheckFilterParams = healthcheckFilterParams;
	}

	@Override
	protected void configureServlets(){
		//dispatcher and routeSet classes
		bind(DispatcherServletClasses.class)
				.toInstance(new DispatcherServletClasses(Arrays.asList(rootDispatcherServletClass)));

		//https configuration implementation
		bind(HttpsConfiguration.class).to(httpsConfigurationClass);

		//filters
		if(healthcheckFilterParams != null){
			filter(healthcheckFilterParams.path).through(healthcheckFilterParams.filterClass);
		}
		List<Class<? extends Filter>> rootFilterClasses = new ArrayList<>();
		if(excludeStaticFileFilter){
			rootFilterClasses.remove(StaticFileFilter.class);
		}
		rootFilterClasses.forEach(this::rootFilter);
		rootFilter(authenticationFilterClass);
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
		serveRegex(guicePathsRegex).with(rootDispatcherServletClass);

		//nav bar
		bindActual(AppNavBar.class, DefaultAppNavBar.class);
	}

	@Provides
	public RootRouteSetsSupplier getRootRouteSets(DatarouterInjector injector){
		List<BaseRouteSet> routeSets = new ArrayList<>();
		routeSets.addAll(IterableTool.map(additionalRootRouteSetClasses, injector::getInstance));
		routeSets.addAll(rootRouteSets);
		return new RootRouteSets(routeSets);
	}

}
