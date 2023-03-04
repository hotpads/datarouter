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
package io.datarouter.httpclient.endpoint.java;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

/**
 * @param <R> response type
 * @param <ET> EndpointType
 */
public abstract class BaseEndpoint<R,ET extends EndpointType>{

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
	@IgnoredField
	public final Map<String,List<String>> headers;

	// initialized by the client
	@IgnoredField
	public String urlPrefix;

	@IgnoredField
	private Optional<Boolean> retrySafe;
	@IgnoredField
	public Optional<Duration> timeout;
	@IgnoredField
	public boolean shouldSkipSecurity;
	@IgnoredField
	public boolean shouldSkipLogs;

	public BaseEndpoint(HttpRequestMethod method, PathNode pathNode, Class<? extends CallerType> callerType){
		this.method = method;
		this.pathNode = pathNode;
		this.headers = new HashMap<>();
		this.callerType = callerType;

		this.urlPrefix = null;
		this.retrySafe = Optional.empty();
		this.timeout = Optional.empty();
		this.shouldSkipSecurity = false;
		this.shouldSkipLogs = false;
	}

	public final BaseEndpoint<R,ET> setUrlPrefix(URI urlPrefix){
		this.urlPrefix = urlPrefix.normalize().toString();
		return this;
	}

	public final BaseEndpoint<R,ET> setTimeout(Duration timeout){
		this.timeout = Optional.of(timeout);
		return this;
	}

	public final BaseEndpoint<R,ET> addHeader(String name, String value){
		headers.computeIfAbsent(name, $ -> new ArrayList<>()).add(value);
		return this;
	}

	public final BaseEndpoint<R,ET> setShouldSkipSecurity(boolean shouldSkipSecurity){
		this.shouldSkipSecurity = shouldSkipSecurity;
		return this;
	}

	public final BaseEndpoint<R,ET> setShouldSkipLogs(boolean shouldSkipLogs){
		this.shouldSkipLogs = shouldSkipLogs;
		return this;
	}

	public final BaseEndpoint<R,ET> setRetrySafe(boolean retrySafe){
		this.retrySafe = Optional.of(retrySafe);
		return this;
	}

	public final boolean getRetrySafe(){
		return retrySafe
				.orElseGet(() -> switch(method){
					case GET -> true;
					case HEAD, DELETE, PATCH, PUT, POST -> false;
				});
	}

}
