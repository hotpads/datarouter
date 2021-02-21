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
	public String urlPrefix;
	@IgnoredField
	public Optional<Boolean> retrySafe;
	@IgnoredField
	public Optional<Duration> timeout;
	@IgnoredField
	public Optional<Object> entity;

	public BaseEndpoint(HttpRequestMethod method, PathNode pathNode, Class<T> responseType){
		this.method = method;
		this.pathNode = pathNode;
		this.responseType = responseType;

		this.urlPrefix = null;
		this.retrySafe = Optional.empty();
		this.timeout = Optional.empty();
		this.entity = Optional.empty();
	}

	public BaseEndpoint<T> setUrlPrefix(URI urlPrefix){
		this.urlPrefix = urlPrefix.normalize().toString();
		return this;
	}

	protected <E> E setRequestBody(E entityDto){
		// needs to be Optional.ofNullable for reflection
		this.entity = Optional.ofNullable(entityDto);
		return entityDto;
	}

	protected void setRetrySafe(boolean retrySafe){
		this.retrySafe = Optional.of(retrySafe);
	}

	protected void setTimeout(Duration timeout){
		this.timeout = Optional.of(timeout);
	}

}
