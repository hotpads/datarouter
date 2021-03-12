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
package io.datarouter.httpclient.endpoint;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

public abstract class BaseEndpoint<T>{

	@IgnoredField
	public final HttpRequestMethod method;
	@IgnoredField
	public final Class<T> responseType;
	@IgnoredField
	public final PathNode pathNode;
	@IgnoredField
	public final boolean retrySafe;

	@IgnoredField
	public String urlPrefix;
	@IgnoredField
	public Optional<Duration> timeout;

	public BaseEndpoint(HttpRequestMethod method, PathNode pathNode, Class<T> responseType, boolean retrySafe){
		this.method = method;
		this.pathNode = pathNode;
		this.responseType = responseType;
		this.retrySafe = retrySafe;

		this.urlPrefix = null;
		this.timeout = Optional.empty();
	}

	public BaseEndpoint<T> setUrlPrefix(URI urlPrefix){
		this.urlPrefix = urlPrefix.normalize().toString();
		return this;
	}

	protected void setTimeout(Duration timeout){
		this.timeout = Optional.of(timeout);
	}

}
