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
package io.datarouter.httpclient.endpoint.web;

import java.net.URI;
import java.util.List;

import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.httpclient.endpoint.caller.CallerTypeInternalFrontEnd;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

/**
 * @param <R> response type
 * @param <T> WebApiType
 */
public abstract class BaseWebApi<R,T extends WebApiType>{

	@IgnoredField
	protected static final HttpRequestMethod GET = HttpRequestMethod.GET;
	@IgnoredField
	protected static final HttpRequestMethod POST = HttpRequestMethod.POST;

	@IgnoredField
	public final HttpRequestMethod method;
	@IgnoredField
	public final PathNode pathNode;
	@IgnoredField
	public final Class<? extends CallerType> callerType;

	// initialized by the client
	@IgnoredField
	public String urlPrefix;

	public BaseWebApi(HttpRequestMethod method, PathNode pathNode, Class<? extends CallerType> callerType){
		this.method = method;
		this.pathNode = pathNode;
		this.callerType = callerType;

		this.urlPrefix = null;
	}

	public BaseWebApi(HttpRequestMethod method, PathNode pathNode){
		this.method = method;
		this.pathNode = pathNode;
		this.callerType = CallerTypeInternalFrontEnd.class;

		this.urlPrefix = null;
	}

	public final BaseWebApi<R,T> setUrlPrefix(URI urlPrefix){
		this.urlPrefix = urlPrefix.normalize().toString();
		return this;
	}

	public abstract List<Class<? extends JsClientType>> getJsClientTypes();

}
