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
package io.datarouter.web.util.http.exception;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.web.exception.HandledException;

@SuppressWarnings("serial")
public class HttpException extends RuntimeException implements HandledException{

	private final int statusCode;
	private List<String> details = new ArrayList<>();

	public HttpException(String message, int statusCode){
		super(message);
		this.statusCode = statusCode;
	}

	public HttpException(String message, Throwable cause, int statusCode){
		super(message, cause);
		this.statusCode = statusCode;
	}

	@Override
	public int getHttpResponseCode(){
		return statusCode;
	}

	@Override
	public Object getHttpResponseBody(){
		Map<String,Object> responseBody = new LinkedHashMap<>();
		responseBody.put("code", statusCode);
		responseBody.put("message", getMessage());
		if(CollectionTool.notEmpty(details)){
			responseBody.put("details", details);
		}
		return responseBody;
	}

	public List<String> getDetails(){
		return details;
	}

	public void setDetails(List<String> details){
		this.details = details;
	}
}
