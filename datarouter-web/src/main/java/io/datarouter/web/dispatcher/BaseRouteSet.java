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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.pathnode.PathNode;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.api.external.BaseExternalEndpoint;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerTool;

public abstract class BaseRouteSet implements RouteSet{
	private static final Logger logger = LoggerFactory.getLogger(BaseRouteSet.class);

	public static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";
	public static final String REGEX_TWO_DIRECTORY_PLUS = "/\\w+/\\w+[/]?.*";
	public static final String MATCHING_ANY = ".*";
	public static final String MATCHING_CHILD_PATHS_AND_QUERY_PARAMS = "((\\?|/).*)?";

	private final List<DispatchRule> dispatchRules;

	public BaseRouteSet(){
		this.dispatchRules = new ArrayList<>();
	}

	/*---------------- create DispatchRules -----------------*/

	protected DispatchRule handle(PathNode pathNode){
		return handle(pathNode.toSlashedString());
	}

	protected DispatchRule handle(String regex){
		DispatchRule rule = new DispatchRule(this, regex);
		return applyDefaultAndAdd(rule);
	}

	protected DispatchRule handle(Class<? extends BaseEndpoint> baseClass){
		if(BaseExternalEndpoint.class.isAssignableFrom(baseClass)){
			throw new IllegalArgumentException(String.format("External endpoints must be registered using "
					+ "handleExternalEndpoint(). Endpoint=%s", baseClass.getSimpleName()));
		}
		BaseEndpoint baseEndpoint = validateEndpoint(baseClass);
		DispatchType dispatchType = EndpointTool.getDispatchTypeForEndpoint(baseClass);
		return handle(baseEndpoint.pathNode)
				.withDispatchType(dispatchType);
	}

	protected DispatchRule handleLink(Class<? extends BaseLink<?>> baseClass){
		BaseLink<?> baseLink = ReflectionTool.createWithoutNoArgs(baseClass);
		return handle(baseLink.pathNode)
				.withDispatchType(DispatchType.INTERNAL_LINK);
	}

	protected DispatchRule handleExternalEndpoint(Class<? extends BaseExternalEndpoint<?,?>> baseClass){
		BaseEndpoint baseEndpoint = validateEndpoint(baseClass);
		return handle(baseEndpoint.pathNode)
				.withDispatchType(DispatchType.EXTERNAL_ENDPOINT);
	}

	private BaseEndpoint validateEndpoint(Class<? extends BaseEndpoint> endpointClass){
		BaseEndpoint baseEndpoint = ReflectionTool.createWithoutNoArgs(endpointClass);
		try{
			EndpointTool.validateBaseEndpoint(baseEndpoint);
		}catch(IllegalArgumentException ex){
			logger.error("", ex); // puts the validation stack trace at the top
			throw ex;
		}
		return baseEndpoint;
	}

	/**
	 * A convenience method to automatically register all JavaEndpoints, MobileEndpoints, WebApis
	 * and Links that are used in a Handler class.
	 * This only works if all the handler methods inside the Handler class are using JavaEndpoints, MobileEndpoints,
	 * WebApis or Links.
	 *
	 * This method works for registration without any custom dispatch rules. The applyDefault is the only dispatch rule
	 * used.
	 *
	 * This method will NOT register ExternalEndpoints. To register those, you must use handlerExternalEndpoint() and
	 * use a persistent string for the dispatch rule.
	 */
	protected void registerHandler(Class<? extends BaseHandler> handler){
		List<Class<? extends BaseEndpoint>> baseEndpoints = HandlerTool.getEndpointsFromHandler(handler);
		baseEndpoints.forEach(baseEndpoint -> handle(baseEndpoint).withHandler(handler));

		List<Class<? extends BaseLink<?>>> links = HandlerTool.getLinksFromHandler(handler);
		links.forEach(link -> handleLink(link).withHandler(handler));
	}

	protected DispatchRule applyDefaultAndAdd(DispatchRule rule){
		applyDefault(rule);
		this.dispatchRules.add(rule);
		return rule;
	}

	/**
	 * @deprecated use handle(PathNode) and explicitly define each path
	 *
	 *             Example:
	 *
	 *             <pre>
	 *             handleDir(api.v1.test).withHandler(TestHandler.class);
	 *             </pre>
	 *
	 *             changes to
	 *             <pre>
	 *             handle(api.v1.test.example1).withHandler(TestHandler.class);
	 *             handle(api.v1.test.example2).withHandler(TestHandler.class);
	 *             handle(api.v1.test.example3).withHandler(TestHandler.class);
	 *             </pre>
	 *
	 */
	@Deprecated
	protected DispatchRule handleDir(PathNode pathNode){
		return handleDir(pathNode.toSlashedString());
	}

	/**
	 * @deprecated use handle(String) and explicitly define each path
	 */
	@Deprecated
	protected DispatchRule handleDir(String regex){
		return handle(regex + REGEX_ONE_DIRECTORY);
	}

	@Deprecated // use handleAnyStringAfterPath
	protected DispatchRule handleAnyPrefix(PathNode pathNode){
		return handleAnyStringAfterPath(pathNode);
	}

	protected DispatchRule handleAnyStringAfterPath(PathNode pathNode){
		return handle(pathNode.toSlashedString() + MATCHING_ANY);
	}

	protected DispatchRule applyDefault(DispatchRule rule){
		return rule;
	}

	protected DispatchRule redirect(PathNode origin, String localRedirectPath){
		return redirect(origin, "", localRedirectPath);
	}

	protected DispatchRule redirect(PathNode origin, String redirectDomain, String redirectPath){
		var rule = new DispatchRule(this, origin.toSlashedString() + MATCHING_CHILD_PATHS_AND_QUERY_PARAMS)
				.withRedirect(redirectDomain + redirectPath);
		this.dispatchRules.add(rule);
		return rule;
	}

	/*------------------ getters -------------------*/

	@Override
	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}

}
