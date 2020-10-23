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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.handler.BaseHandler;

public abstract class BaseRouteSet{

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

	protected DispatchRule applyDefaultAndAdd(DispatchRule rule){
		applyDefault(rule);
		this.dispatchRules.add(rule);
		return rule;
	}

	protected DispatchRule handleDir(PathNode regex){
		return handleDir(regex.toSlashedString());
	}

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
