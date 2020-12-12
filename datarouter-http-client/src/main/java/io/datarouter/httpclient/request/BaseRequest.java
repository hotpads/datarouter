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

import org.apache.http.entity.ContentType;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.pathnode.PathNode;

public abstract class BaseRequest<T> extends DatarouterHttpRequest{

	public final Class<T> responseType;

	public BaseRequest(HttpRequestMethod method, PathNode path, Class<T> responseType){
		this(method, path.toSlashedString(), responseType);
	}

	public BaseRequest(HttpRequestMethod method, String path, Class<T> responseType){
		super(method, path);
		this.responseType = responseType;
	}

	protected void addParam(String key, String value){
		addParam(key, value);
	}

	protected void addIntParam(String key, int value){
		addParam(key, value + "");
	}

	protected void addLongParam(String key, long value){
		addParam(key, value + "");
	}

	protected void setEntityDto(JsonSerializer jsonSerializer, Object dto){
		String serializedDto = jsonSerializer.serialize(dto);
		setEntity(serializedDto, ContentType.APPLICATION_JSON);
	}

}
