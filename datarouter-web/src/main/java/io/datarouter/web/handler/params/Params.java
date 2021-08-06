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
package io.datarouter.web.handler.params;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import io.datarouter.util.BooleanTool;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.util.http.RequestTool;

public class Params{

	private final HttpServletRequest request;
	protected final Map<String,String> paramsMap;

	public Params(HttpServletRequest request){
		this.request = request;
		this.paramsMap = RequestTool.getParamMap(request);
	}

	public String required(String key){
		return Objects.requireNonNull(paramsMap.get(key));
	}

	public Optional<String> optional(String key){
		return Optional.ofNullable(paramsMap.get(key));
	}

	public String optional(String key, String defaultValue){
		return optional(key).orElse(defaultValue);
	}

	public Optional<String> optionalNotEmpty(String key){
		return optional(key).filter(StringTool::notEmptyNorWhitespace);
	}

	public Boolean requiredBoolean(String key){
		return BooleanTool.isTrue(required(key));
	}

	public Boolean optionalBoolean(String key, Boolean defaultValue){
		return optional(key)
				.map(BooleanTool::isTrue)
				.orElse(defaultValue);
	}

	public Optional<Boolean> optionalBoolean(String key){
		return optional(key).map(Boolean::valueOf);
	}

	public Long requiredLong(String key){
		return Long.valueOf(required(key));
	}

	public Optional<Long> optionalLong(String key){
		return optional(key).map(Long::valueOf);
	}

	public Long optionalLong(String key, Long defaultValue){
		return optionalLong(key).orElse(defaultValue);
	}

	public Long optionalLongEmptySafe(String key, Long defaultValue){
		return optional(key)
				.filter(StringTool::notNullNorEmptyNorWhitespace)
				.map(Long::valueOf)
				.orElse(defaultValue);
	}

	public Optional<Long> optionalLongSafeParsing(String key){
		return optional(key)
				.map(value -> NumberTool.getLongNullSafe(value, null))
				.filter(Objects::nonNull);
	}

	public Integer requiredInteger(String key){
		return Integer.valueOf(required(key));
	}

	public Integer optionalInteger(String key, Integer defaultValue){
		return optional(key)
				.filter(StringTool::notNullNorEmptyNorWhitespace)
				.map(Integer::valueOf)
				.orElse(defaultValue);
	}

	public Optional<Integer> optionalInteger(String key){
		return optional(key).map(Integer::valueOf);
	}

	public Double requiredDouble(String key){
		return Double.valueOf(required(key));
	}

	public Double optionalDouble(String key, Double defaultValue){
		return optional(key)
				.map(Double::valueOf)
				.orElse(defaultValue);
	}

	public List<String> optionalCsvList(String key, List<String> defaultValue){
		return optionalList(key, ",", defaultValue);
	}

	public List<String> optionalList(String key, String delimiter, List<String> defaultValue){
		return optional(key)
				.map(str -> str.split(delimiter))
				.map(Arrays::asList)
				.orElse(defaultValue);
	}

	public Optional<String[]> optionalArray(String key){
		return Optional.ofNullable(request.getParameterValues(key));
	}

	/**
	 * This does not guarantee multiple elements
	 */
	public String[] requiredArray(String key){
		return Objects.requireNonNull(request.getParameterValues(key));
	}

	public Integer tryGetInteger(String key, Integer defaultValue){
		try{
			return optionalInteger(key, defaultValue);
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}

	public Double tryGetDouble(String key, Double defaultValue){
		try{
			return optionalDouble(key, defaultValue);
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}

	public Long tryGetLong(String key, Long defaultValue){
		try{
			return optionalLong(key, defaultValue);
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}

	public Date tryGetLongAsDate(String key, Long defaultValue){
		Long value = tryGetLong(key, defaultValue);
		return new Date(value);
	}

	public FileItem requiredFile(@SuppressWarnings("unused") String key){
		throw new RuntimeException("not a multipart request");
	}

	public Optional<FileItem> optionalFile(@SuppressWarnings("unused") String key){
		return Optional.empty();
	}

	public Map<String,String> toMap(){
		return paramsMap;
	}

	/*--------------------------- get/set -----------------------------------*/

	public HttpServletRequest getRequest(){
		return request;
	}

}
