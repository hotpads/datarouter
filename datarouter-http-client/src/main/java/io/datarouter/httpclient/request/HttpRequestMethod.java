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

import java.util.function.Function;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

public enum HttpRequestMethod{
	DELETE(
			false,
			true,
			DatarouterHttpDeleteRequestWithEntity::new),
	GET(
			true,
			false,
			HttpGet::new),
	HEAD(
			false,
			false,
			HttpHead::new),
	PATCH(
			false,
			true,
			HttpPatch::new),
	POST(
			false,
			true,
			HttpPost::new),
	PUT(
			false,
			true,
			HttpPut::new),
	;

	public final boolean defaultRetrySafe;
	public final boolean allowEntity;
	public final Function<String,HttpRequestBase> httpRequestBase;

	HttpRequestMethod(boolean defaultRetrySafe, boolean allowEntity,
			Function<String,HttpRequestBase> httpRequestBase){
		this.defaultRetrySafe = defaultRetrySafe;
		this.allowEntity = allowEntity;
		this.httpRequestBase = httpRequestBase;
	}

}
