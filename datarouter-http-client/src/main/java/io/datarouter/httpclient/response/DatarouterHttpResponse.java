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
package io.datarouter.httpclient.response;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.client.DatarouterConnectionSocketFactory;

/**
 * This class is an abstraction over the HttpResponse that handles several of the expected HTTP failures
 */
public class DatarouterHttpResponse{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpResponse.class);

	private final HttpResponse response;
	private final List<Cookie> cookies;
	private final HttpRequest request;
	private final InetSocketAddress remoteAddress;
	private final int statusCode;
	private final String entity;

	public DatarouterHttpResponse(HttpResponse response, HttpClientContext context, int statusCode, String entity){
		this.response = response;
		this.cookies = context.getCookieStore().getCookies();
		this.request = context.getRequest();
		this.remoteAddress = (InetSocketAddress)context.getAttribute(DatarouterConnectionSocketFactory.REMOTE_ADDRESS);
		this.statusCode = statusCode;
		this.entity = entity;
	}

	public int getStatusCode(){
		return statusCode;
	}

	public String getEntity(){
		return entity;
	}

	public Header getFirstHeader(String name){
		return response.getFirstHeader(name);
	}

	public Header[] getHeaders(String name){
		return response.getHeaders(name);
	}

	public Header[] getAllHeaders(){
		return response.getAllHeaders();
	}

	public List<Cookie> getCookies(){
		return cookies;
	}

	public HttpRequest getRequest(){
		return request;
	}

	// non null only for initial request
	public InetSocketAddress getRemoteAddress(){
		return remoteAddress;
	}

	public StatusLine getStatusLine(){
		return response.getStatusLine();
	}

	public void tryClose(){
		try{
			HttpEntity entity = response.getEntity();
			if(entity == null){
				//response had no entity to close, as in a response to a HEAD request
				return;
			}
			entity.getContent().close();
		}catch(Exception e){
			logger.warn("failed to close", e);
		}
	}
}
