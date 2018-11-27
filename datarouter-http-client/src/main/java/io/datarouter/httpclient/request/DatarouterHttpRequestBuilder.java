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

import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClientSettings;
import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;

public class DatarouterHttpRequestBuilder{

	private final DatarouterHttpClientSettings settings;
	private final DatarouterHttpClient httpClient;

	public DatarouterHttpRequestBuilder(DatarouterHttpClientSettings settings, DatarouterHttpClient httpClient){
		this.settings = settings;
		this.httpClient = httpClient;
	}

	/*---------- get ------------*/

	public DatarouterHttpRequest createGet(String path){
		return new DatarouterHttpRequest(HttpRequestMethod.GET, buildUrl(path), true);
	}

	public DatarouterHttpRequest createGet(PathNode pathNode){
		return createGet(pathNode.toSlashedString());
	}

	/*---------- post ------------*/

	public DatarouterHttpRequest createPost(PathNode pathNode){
		return createPost(pathNode.toSlashedString());
	}

	public DatarouterHttpRequest createPost(PathNode pathNode, Object entityDto){
		return createPost(pathNode.toSlashedString(), entityDto);
	}

	public DatarouterHttpRequest createPost(String path){
		return new DatarouterHttpRequest(HttpRequestMethod.POST, buildUrl(path), false);
	}

	public DatarouterHttpRequest createPost(String path, Object entityDto){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.POST, buildUrl(path), false);
		httpClient.setEntityDto(request, entityDto);
		return request;
	}

	/*---------- put ------------*/

	public DatarouterHttpRequest createPut(String path){
		return new DatarouterHttpRequest(HttpRequestMethod.PUT, buildUrl(path), true);
	}

	public DatarouterHttpRequest createDelete(String path){
		return new DatarouterHttpRequest(HttpRequestMethod.DELETE, buildUrl(path), true);
	}

	/*---------- other ------------*/

	public String buildUrl(String path){
		URI endpointUrl = settings.getEndpointUrl();
		URI finalUrl = URI.create(endpointUrl + "/" + path);
		return finalUrl.normalize().toString();
	}

}
