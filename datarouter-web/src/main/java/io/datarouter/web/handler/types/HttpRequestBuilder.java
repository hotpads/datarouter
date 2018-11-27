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
package io.datarouter.web.handler.types;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

public class HttpRequestBuilder{

	private final Map<String,String[]> parameterMap = new HashMap<>();
	private Reader reader;
	private final Map<String,Set<String>> headers = new HashMap<>();
	private final Map<String, Object> attributes = new HashMap<>();
	private final List<Cookie> cookies = new ArrayList<>();
	private String serverName;
	private String method;

	public MockHttpRequest build(){
		return new MockHttpRequest(parameterMap, reader, headers, attributes, cookies, serverName, method);
	}

	public HttpRequestBuilder withParameter(String key, String value){
		parameterMap.put(key, new String[]{value});
		return this;
	}

	public HttpRequestBuilder withArrayParameter(String key, String... value){
		parameterMap.put(key, value);
		return this;
	}

	public HttpRequestBuilder withBody(String string){
		reader = new StringReader(string);
		return this;
	}

	public HttpRequestBuilder withHeader(String name, String value){
		headers.computeIfAbsent(name, $ -> new LinkedHashSet<>()).add(value);
		return this;
	}

	public HttpRequestBuilder withAttribute(String key, Object value){
		attributes.put(key, value);
		return this;
	}

	public HttpRequestBuilder withCookie(Cookie cookie){
		cookies.add(cookie);
		return this;
	}

	public HttpRequestBuilder withServerName(String serverName){
		this.serverName = serverName;
		return this;
	}

	public HttpRequestBuilder withMethod(String method){
		this.method = method;
		return this;
	}
}
