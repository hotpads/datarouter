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

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

public abstract class BaseEndpoint<T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseEndpoint.class);

	@IgnoredField
	private final HttpRequestMethod method;
	@IgnoredField
	public final Class<T> responseType;
	@IgnoredField
	public final PathNode pathNode;

	@IgnoredField
	private String urlPrefix;
	@IgnoredField
	private Optional<Boolean> retrySafe;
	@IgnoredField
	private Optional<Duration> timeout;
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

	protected <E> E setEntityDto(E entityDto){
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

	public static DatarouterHttpRequest toDatarouterHttpRequest(BaseEndpoint<?> endpoint){
		Objects.requireNonNull(endpoint.urlPrefix);
		String finalUrl = URI.create(endpoint.urlPrefix + endpoint.pathNode.toSlashedString()).normalize().toString();
		var request = new DatarouterHttpRequest(endpoint.method, finalUrl);
		endpoint.retrySafe.ifPresent(request::setRetrySafe);
		endpoint.timeout.ifPresent(request::setTimeout);
		for(Field field : endpoint.getClass().getFields()){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			String key = field.getName();
			Object value = null;
			try{
				value = field.get(endpoint);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			EndpointParam param = field.getAnnotation(EndpointParam.class);
			Optional<String> parsedValue = getValue(field, value);
			if(param == null){
				parsedValue.ifPresent(paramValue -> request.addParam(key, paramValue));
				continue;
			}
			ParamType paramType = param.paramType();
			if(paramType == ParamType.GET){
				parsedValue.ifPresent(paramValue -> request.addGetParam(key, paramValue));
			}else if(paramType == ParamType.POST){
				parsedValue.ifPresent(paramValue -> request.addPostParam(key, paramValue));
			}
		}
		return request;
	}

	private static Optional<String> getValue(Field field, Object value){
		if(!field.getType().isAssignableFrom(Optional.class)){
			return Optional.of(value.toString());
		}
		Optional<?> optionalValue = (Optional<?>)value;
		if(optionalValue.isPresent()){
			return Optional.of(optionalValue.get().toString());
		}
		return Optional.empty();
	}

}
