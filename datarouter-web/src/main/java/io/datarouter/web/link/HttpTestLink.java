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
package io.datarouter.web.link;

import java.util.Optional;

import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebPaths;

public class HttpTestLink extends DatarouterLink{

	public Optional<String> url = Optional.empty();
	public Optional<String> method = Optional.empty();
	public Optional<String> requestBody = Optional.empty();
	public Optional<String> headers = Optional.empty();
	public Optional<String> contentType = Optional.empty();
	public Optional<String> useProxy = Optional.empty();
	public Optional<String> followRedirects = Optional.empty();

	public HttpTestLink(PathNode pathNode){
		super(new DatarouterWebPaths().datarouter.http.tester);
	}

	public HttpTestLink withUrl(String url){
		this.url = Optional.ofNullable(url);
		return this;
	}

	public HttpTestLink withMethod(String method){
		this.method = Optional.ofNullable(method);
		return this;
	}

	public HttpTestLink withRequestBody(String requestBody){
		this.requestBody = Optional.ofNullable(requestBody);
		return this;
	}

	public HttpTestLink withHeaders(String headers){
		this.headers = Optional.ofNullable(headers);
		return this;
	}

	public HttpTestLink withContentType(String contentType){
		this.contentType = Optional.ofNullable(contentType);
		return this;
	}

	public HttpTestLink withUseProxy(String useProxy){
		this.useProxy = Optional.ofNullable(useProxy);
		return this;
	}

	public HttpTestLink withFollowRedirects(String followRedirects){
		this.followRedirects = Optional.ofNullable(followRedirects);
		return this;
	}

}
