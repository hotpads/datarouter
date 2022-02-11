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
package io.datarouter.httpclient.request;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

public enum HttpRequestMethod{
	DELETE(
			"DELETE",
			false,
			true,
			DatarouterHttpDeleteRequestWithEntity::new),
	GET(
			"GET",
			true,
			false,
			HttpGet::new),
	HEAD(
			"HEAD",
			false,
			false,
			HttpHead::new),
	PATCH(
			"PATCH",
			false,
			true,
			HttpPatch::new),
	POST(
			"POST",
			false,
			true,
			HttpPost::new),
	PUT(
			"PUT",
			false,
			true,
			HttpPut::new),
	;

	public final String persistentString;
	public final boolean defaultRetrySafe;
	public final boolean allowEntity;
	public final Function<String,HttpRequestBase> httpRequestBase;


	HttpRequestMethod(
			String persistentString,
			boolean defaultRetrySafe,
			boolean allowEntity,
			Function<String,HttpRequestBase> httpRequestBase){
		this.persistentString = persistentString;
		this.defaultRetrySafe = defaultRetrySafe;
		this.allowEntity = allowEntity;
		this.httpRequestBase = httpRequestBase;
	}

	public boolean matches(String method){
		return fromPersistentStringStatic(method) == this;
	}

	public static HttpRequestMethod fromPersistentStringStatic(String method){
		if(method == null || method.isEmpty()){
			return null;
		}
		String methodUpperCase = method.toUpperCase();
		return Arrays.stream(values())
				.filter(requestMethod -> requestMethod.persistentString.equals(methodUpperCase.toUpperCase()))
				.findFirst()
				.orElse(null);
	}

}
