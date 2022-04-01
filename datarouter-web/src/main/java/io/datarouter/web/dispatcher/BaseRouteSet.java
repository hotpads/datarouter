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
import io.datarouter.httpclient.endpoint.BaseInternalLink;
import io.datarouter.httpclient.endpoint.EndpointTool;
import io.datarouter.pathnode.PathNode;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerTool;
import io.datarouter.web.handler.types.InternalLinkDecoder;

public abstract class BaseRouteSet{
	private static final Logger logger = LoggerFactory.getLogger(BaseRouteSet.class);

	public static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";
	public static final String REGEX_TWO_DIRECTORY_PLUS = "/\\w+/\\w+[/]?.*";
	public static final String MATCHING_ANY = ".*";

	private final String urlPrefix;
	private final List<DispatchRule> dispatchRules;

	private Class<? extends BaseHandler> defaultHandlerClass;

	public BaseRouteSet(String urlPrefix){
		this.urlPrefix = urlPrefix;
		this.dispatchRules = new ArrayList<>();
	}

	public BaseRouteSet(PathNode pathNode){
		this(pathNode.toSlashedString());
	}

	/*---------------- create DispatchRules -----------------*/

	protected DispatchRule handle(PathNode regex){
		return handle(regex.toSlashedString());
	}

	protected DispatchRule handle(String regex){
		DispatchRule rule = new DispatchRule(this, regex);
		return applyDefaultAndAdd(rule);
	}

	protected DispatchRule handle(Class<? extends BaseEndpoint<?,?>> baseEndpointClass){
		BaseEndpoint<?,?> baseEndpoint = ReflectionTool.createWithoutNoArgs(baseEndpointClass);
		try{
			EndpointTool.validateEndpoint(baseEndpoint);
		}catch(IllegalArgumentException ex){
			logger.error("", ex); // puts the validation stack trace at the top
			throw ex;
		}
		return handle(baseEndpoint.pathNode)
				.withDispatchType(DispatchType.API_ENDPOINT);
	}

	/**
	 * A convenience method to automatically register all Endpoints that are used in a Handler class. This only works if
	 * all the handler methods inside the Handler class are using endpoints.
	 *
	 * This method works for registration without any custom dispatch rules. The applyDefault is the only dispatch rule
	 * used.
	 */
	protected void registerHandler(Class<? extends BaseHandler> handler){
		List<Class<? extends BaseEndpoint<?,?>>> endpoints = HandlerTool.getEndpointsFromHandler(handler);
		endpoints.forEach(endpoint -> handle(endpoint).withHandler(handler));
	}

	protected DispatchRule handleInternalLink(
			Class<? extends BaseInternalLink> baseInternalLink,
			Class<? extends BaseHandler> handler){
		BaseInternalLink link = ReflectionTool.createWithoutNoArgs(baseInternalLink);
		return handle(link.pathNode)
				.withDefaultHandlerDecoder(InternalLinkDecoder.class)
				.withHandler(handler);
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

	protected DispatchRule handleAnySuffix(String suffix){
		return handle(MATCHING_ANY + suffix);
	}

	protected DispatchRule handleAnyPrefix(PathNode prefix){
		return handleAnyPrefix(prefix.toSlashedString());
	}

	protected DispatchRule handleAnyPrefix(String prefix){
		return handle(prefix + MATCHING_ANY);
	}

	protected DispatchRule applyDefault(DispatchRule rule){
		return rule;
	}

	/*------------------ getters -------------------*/

	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}

	public String getUrlPrefix(){
		return urlPrefix;
	}

	public Class<? extends BaseHandler> getDefaultHandlerClass(){
		return defaultHandlerClass;
	}

}
