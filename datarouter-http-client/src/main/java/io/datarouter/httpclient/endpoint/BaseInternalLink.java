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
package io.datarouter.httpclient.endpoint;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.pathnode.PathNode;

public class BaseInternalLink{
	private static final Logger logger = LoggerFactory.getLogger(BaseInternalLink.class);

	@IgnoredField
	public final PathNode pathNode;
	@IgnoredField
	public final UrlLinkRoot urlLinkRoot;

	public BaseInternalLink(UrlLinkRoot urlLinkRoot, PathNode pathNode){
		this.urlLinkRoot = urlLinkRoot;
		this.pathNode = pathNode;
	}

	public String getParamsAsString(){
		List<String> params = new LinkedList<>();
		for(Field field : getClass().getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			EndpointParam endpointParam = field.getAnnotation(EndpointParam.class);
			String key = Optional.ofNullable(endpointParam)
					.map(EndpointParam::serializedName)
					.filter(name -> !name.isEmpty())
					.orElseGet(field::getName);
			Object value = null;
			try{
				value = field.get(this);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			EndpointParam param = field.getAnnotation(EndpointParam.class);
			Optional<String> parsedValue = getValue(field, value);
			if(param == null || param.paramType() == null){
				parsedValue.ifPresent(paramValue -> {
					String keyValue = key + "=" + paramValue;
					params.add(keyValue);
				});
				continue;
			}
		}
		return params.stream().collect(Collectors.joining("&", "?", ""));
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
