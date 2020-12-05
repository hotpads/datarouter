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

import java.util.LinkedHashMap;
import java.util.Map;

import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

public abstract class BaseRequest<T>{

	public final HttpRequestMethod method;
	public final String path;
	public final Class<T> responseType;

	public final Map<String,String> params;

	public BaseRequest(HttpRequestMethod method, PathNode path, Class<T> responseType){
		this(method, path.toSlashedString(), responseType);
	}

	public BaseRequest(HttpRequestMethod method, String path, Class<T> responseType){
		this.method = method;
		this.path = path;
		this.responseType = responseType;
		this.params = new LinkedHashMap<>();
	}

	protected void addParam(String key, String value){
		params.put(key, value);
	}

	protected void addIntParam(String key, int value){
		params.put(key, value + "");
	}

	protected void addLongParam(String key, long value){
		params.put(key, value + "");
	}

}
