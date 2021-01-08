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
package io.datarouter.httpclient.request;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.pathnode.PathNode;

public abstract class BaseEndpoint<T>{

	private final HttpRequestMethod method;
	public final Class<T> responseType;
	public final PathNode pathNode;
	private final Map<String,String> params;

	private String urlPrefix;
	private Optional<Boolean> retrySafe;
	private Optional<Duration> timeout;
	public Optional<Object> entity;

	public BaseEndpoint(HttpRequestMethod method, PathNode pathNode, Class<T> responseType){
		this.method = method;
		this.pathNode = pathNode;
		this.responseType = responseType;
		this.params = new LinkedHashMap<>();

		this.urlPrefix = null;
		this.retrySafe = Optional.empty();
		this.timeout = Optional.empty();
		this.entity = Optional.empty();
	}

	protected void addParam(String key, String value){
		params.put(key, value);
	}

	protected void addIntParam(String key, int value){
		addParam(key, value + "");
	}

	protected void addLongParam(String key, long value){
		addParam(key, value + "");
	}

	public BaseEndpoint<T> setUrlPrefix(URI urlPrefix){
		this.urlPrefix = urlPrefix.normalize().toString();
		return this;
	}

	protected BaseEndpoint<T> setEntityDto(Object entity){
		this.entity = Optional.of(entity);
		return this;
	}

	protected void setRetrySafe(boolean retrySafe){
		this.retrySafe = Optional.of(retrySafe);
	}

	protected void setTimeout(Duration timeout){
		this.timeout = Optional.of(timeout);
	}

	public DatarouterHttpRequest toDatarouterHttpRequest(){
		Objects.requireNonNull(urlPrefix);
		String finalUrl = URI.create(urlPrefix + pathNode.toSlashedString()).normalize().toString();
		var request = new DatarouterHttpRequest(method, finalUrl);
		params.forEach((name, value) -> request.addParam(name, value));
		retrySafe.ifPresent(request::setRetrySafe);
		timeout.ifPresent(request::setTimeout);
		return request;
	}

}
