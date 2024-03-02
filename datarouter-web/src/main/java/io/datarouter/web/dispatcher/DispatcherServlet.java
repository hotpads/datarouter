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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.types.JsonAwareHandlerDecoder;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DatarouterNavBar;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSampleDao;
import io.datarouter.web.storage.payloadsampling.response.ResponsePayloadSample;
import io.datarouter.web.storage.payloadsampling.response.ResponsePayloadSampleDao;
import jakarta.inject.Inject;

@SuppressWarnings("serial")
public abstract class DispatcherServlet extends HttpServlet{
	private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

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
	@Inject
	private RequestPayloadSampleDao requestPayloadSampleDao;
	@Inject
	private ResponsePayloadSampleDao responsePayloadSampleDao;
	@Inject
	private Gson gson;
	@Inject
	private ServletContextSupplier servletContext;

	private final List<RouteSet> routeSets = new ArrayList<>();

	@Override
	public void init(){
		registerRouteSets();
		ensureUniqueDispatchRules();
		if(datarouterWebSettingRoot.validateCompatibilityFromSampledPayloads.get()){
			validatePayloadsBackwardCompatibility();
		}
		getInitListeners().forEach(listener -> listener.onDispatcherServletInit(this));
	}

	@Override
	public void destroy(){
		getInitListeners().forEach(listener -> listener.onDispatcherServletDestroy(this));
	}

	protected abstract void registerRouteSets();

	protected List<DispatcherServletListener> getInitListeners(){
		List<DispatcherServletListener> initListeners = new ArrayList<>();
		appNavBar.ifPresent(initListeners::add);
		initListeners.add(datarouterNavBar);
		return initListeners;
	}

	protected final void register(RouteSet newRouteSet){
		routeSets.add(newRouteSet);
	}

	protected final void eagerlyInitRouteSet(BaseRouteSet routeSet){
		// init handlers once, as a sort of eager health check
		routeSet.getDispatchRulesNoRedirects().stream()
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
		case NOT_FOUND -> {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType("text/plain");
			try(PrintWriter out = response.getWriter()){
				out.print(getClass().getCanonicalName() + " could not find Handler for " + request.getRequestURI());
			}
		}
		case FORBIDDEN -> response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		case ROUTED -> {}
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
				.map(rule -> new RegexAndHandler(rule.getRegex(), rule.getHandlerClass()))
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

	private void validatePayloadsBackwardCompatibility(){
		String contextPath = servletContext.get().getContextPath();
		List<PayloadSampleKey> allSampledKeys = requestPayloadSampleDao.scanKeys().list();
		Scanner.of(routeSets)
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.exclude(DispatchRule::getSkipBackwardCompatibilityChecking)
				.toMap(Function.identity(), rule -> findMatchedPaths(rule, allSampledKeys, contextPath))
				.forEach(this::verifySampledPayloads);
	}

	public static List<PayloadSampleKey> findMatchedPaths(DispatchRule dispatchRule, List<PayloadSampleKey> keys,
			String contextPath){
		return Scanner.of(keys)
				.include(key -> {
					String afterContextPath = key.getPath().substring(contextPath.length());
					return dispatchRule.getPattern().matcher(afterContextPath).matches();
				})
				.list();
	}

	private void verifySampledPayloads(DispatchRule rule, List<PayloadSampleKey> keys){
		if(keys.isEmpty()){
			return;
		}
		requestPayloadSampleDao.scanMulti(keys)
				.forEach(reqPayload -> {
						String reqBody = getBody(reqPayload.getBinaryBody(), reqPayload.getEncoding());
						Map<String,String[]> params = gson.fromJson(reqPayload.getParameterMap(),
								new TypeToken<Map<String,String[]>>(){}.getType());

						// create a mock http request using sampled path, request params and body, then try
						// guessing the best matching handler.
						// BaseHandler.getHandlerMethodAndArgs() will try to decode the params and body from
						// the sampled request. It will fail if decoder fails to decode to any field a type.
						Optional<BaseHandler> handler = dispatcher.estimateHandlerForPathAndParams(reqPayload.getKey()
								.getPath(), rule, params, reqBody);
						if(handler.isEmpty()){
							logger.warn("Cannot find handler for path={}", reqPayload.getKey().getPath());
							return;
						}

						Method method = null;
						try{
							method = handler.get().getHandlerMethodAndArgs().method();
							logger.debug("handler method={} request params or body verified.", method.toString());
						}catch(Exception e){
							if(datarouterWebSettingRoot.shouldThrowExceptionForBackwardIncompatibility.get()){
								throw new RuntimeException("request params or body of path="
										+ reqPayload.getKey().getPath() + " is not backward compatible.", e);
							}else{
								logger.warn("request params or body of path=" + reqPayload.getKey().getPath()
										+ " is not backward compatible.", e);
							}
						}

						if(method == null){
							return;
						}
						// verify response body - only for Endpoints or WebApis
						if(!EndpointTool.paramIsEndpointObject(method) && !EndpointTool.paramIsWebApiObject(method)){
							return;
						}
						ResponsePayloadSample response = responsePayloadSampleDao.get(reqPayload.getKey());
						String resBody = getBody(response.getBinaryBody(), response.getEncoding());
						try{
							JsonAwareHandlerDecoder decoder = (JsonAwareHandlerDecoder)handler.get()
									.getHandlerDecoder(method);
							decoder.getJsonSerializer().deserialize(resBody, method.getReturnType());
							logger.debug("handler method={} response body verified.", method.toString());
						}catch(Exception e){
							if(datarouterWebSettingRoot.shouldThrowExceptionForBackwardIncompatibility.get()){
								throw new RuntimeException("response body of path=" + response.getKey().getPath()
										+ " is not backward compatible.", e);
							}else{
								logger.warn("response body of path=" + response.getKey().getPath()
										+ " is not backward compatible.", e);
							}
						}
				});
	}

	public static String getBody(byte[] binaryBody, String encoding){
		String body = null;
		if(binaryBody != null && binaryBody.length > 0){
			try{
				body = new String(binaryBody, encoding);
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException("failed to decode byte array body to string", e);
			}
		}
		return body;
	}


	private record RegexAndHandler(
			String regex,
			Class<? extends BaseHandler> handlerClass){
	}

}
