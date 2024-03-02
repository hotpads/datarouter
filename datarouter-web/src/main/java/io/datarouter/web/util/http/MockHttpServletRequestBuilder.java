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
package io.datarouter.web.util.http;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

import io.datarouter.web.util.RequestAttributeKey;

public class MockHttpServletRequestBuilder{

	private final Map<String,String[]> parameterMap = new HashMap<>();
	private Reader reader;
	private final Map<String,Set<String>> headers = new HashMap<>();
	private final Map<String, Object> attributes = new HashMap<>();
	private final List<Cookie> cookies = new ArrayList<>();
	private String serverName;
	private String method;
	private String requestUri;

	public MockHttpServletRequest build(){
		return new MockHttpServletRequest(
				parameterMap,
				reader,
				headers,
				attributes,
				cookies,
				serverName,
				method,
				requestUri);
	}

	public MockHttpServletRequestBuilder withParameters(Map<String,String[]> parameters){
		parameterMap.putAll(parameters);
		return this;
	}

	public MockHttpServletRequestBuilder withParameter(String key, String value){
		parameterMap.put(key, new String[]{value});
		return this;
	}

	public MockHttpServletRequestBuilder withArrayParameter(String key, String... value){
		parameterMap.put(key, value);
		return this;
	}

	public MockHttpServletRequestBuilder withBody(String string){
		reader = new StringReader(string);
		return this;
	}

	public MockHttpServletRequestBuilder withHeader(String name, String value){
		headers.computeIfAbsent(name, $ -> new LinkedHashSet<>()).add(value);
		return this;
	}

	public <T> MockHttpServletRequestBuilder withAttribute(RequestAttributeKey<T> key, T value){
		attributes.put(key.name(), value);
		return this;
	}

	public MockHttpServletRequestBuilder withCookie(Cookie cookie){
		cookies.add(cookie);
		return this;
	}

	public MockHttpServletRequestBuilder withServerName(String serverName){
		this.serverName = serverName;
		return this;
	}

	public MockHttpServletRequestBuilder withMethod(String method){
		this.method = method;
		return this;
	}

	public MockHttpServletRequestBuilder withRequestUri(String requestUri){
		this.requestUri = requestUri;
		return this;
	}
}
