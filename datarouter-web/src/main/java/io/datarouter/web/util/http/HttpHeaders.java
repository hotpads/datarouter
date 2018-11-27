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
package io.datarouter.web.util.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
import com.google.common.collect.TreeMultimap;

import io.datarouter.httpclient.IpExtractor;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.util.serialization.GsonTool;

public class HttpHeaders{

	public static final String ACCEPT_CHARSET = "accept-charset";
	public static final String ACCEPT_ENCODING = "accept-encoding";
	public static final String ACCEPT_LANGUAGE = "accept-language";
	public static final String ACCEPT = "accept";
	public static final String CACHE_CONTROL = "cache-control";
	public static final String CONNECTION = "connection";
	public static final String CONTENT_ENCODING = "content-encoding";
	public static final String CONTENT_LANGUAGE = "content-language";
	public static final String CONTENT_LENGTH = "content-length";
	public static final String CONTENT_TYPE = "content-type";
	public static final String COOKIE = "cookie";
	public static final String DNT = "dnt";
	public static final String HOST = "host";
	public static final String IF_MODIFIED_SINCE = "if-modified-since";
	public static final String ORIGIN = "origin";
	public static final String PRAGMA = "pragma";
	public static final String REFERER = "referer";
	public static final String UPGRADE = "upgrade";
	public static final String USER_AGENT = "user-agent";
	public static final String SEC_WEBSOCKET_VERSION = "sec-websocket-version";
	public static final String X_CLIENT_IP = IpExtractor.X_CLIENT_IP;
	public static final String X_FORWARDED_FOR = "x-forwarded-for";
	public static final String X_REQUESTED_WITH = "x-requested-with";
	public static final String X_EXCEPTION_ID = DatarouterHttpResponseException.X_EXCEPTION_ID;

	private static final String[] RECORDED_HEADERS = {
			ACCEPT_CHARSET,
			ACCEPT_ENCODING,
			ACCEPT_LANGUAGE,
			ACCEPT,
			CACHE_CONTROL,
			CONNECTION,
			CONTENT_ENCODING,
			CONTENT_LANGUAGE,
			CONTENT_LENGTH,
			CONTENT_TYPE,
			COOKIE,
			DNT,
			HOST,
			IF_MODIFIED_SINCE,
			ORIGIN,
			PRAGMA,
			REFERER,
			USER_AGENT,
			X_FORWARDED_FOR,
			X_REQUESTED_WITH,
	};

	private final Map<String,String> headerMap = new HashMap<>();

	public HttpHeaders(HttpServletRequest request){
		if(request == null){
			return;
		}
		Joiner listJoiner = Joiner.on(", ");
		List<String> tmpHeaders = Collections.list(request.getHeaderNames());
		for(String headerKey : RECORDED_HEADERS){
			if(tmpHeaders.remove(headerKey)){
				headerMap.put(headerKey, listJoiner.join(Collections.list(request.getHeaders(headerKey))));
			}
		}
		Map<String,List<String>> others = tmpHeaders.stream()
				.collect(Collectors.toMap(Function.identity(), name -> Collections.list(request.getHeaders(name))));
		headerMap.put("others", GsonTool.GSON.toJson(others));
	}

	public HttpHeaders(TreeMultimap<String,String> sortedHeaders){
		if(sortedHeaders == null){
			return;
		}
		Joiner listJoiner = Joiner.on(", ");
		List<String> tmpHeaders = new ArrayList<>(sortedHeaders.keySet());
		for(String headerKey : RECORDED_HEADERS){
			if(tmpHeaders.remove(headerKey)){
				this.headerMap.put(headerKey, listJoiner.join(sortedHeaders.get(headerKey)));
			}
		}
		Map<String,Collection<String>> others = tmpHeaders.stream()
				.collect(Collectors.toMap(Function.identity(), sortedHeaders::get));
		this.headerMap.put("others", GsonTool.GSON.toJson(others));
	}

	public String getAcceptCharset(){
		return headerMap.get(ACCEPT_CHARSET);
	}

	public String getAcceptEncoding(){
		return headerMap.get(ACCEPT_ENCODING);
	}

	public String getAcceptLanguage(){
		return headerMap.get(ACCEPT_LANGUAGE);
	}

	public String getAccept(){
		return headerMap.get(ACCEPT);
	}

	public String getCacheControl(){
		return headerMap.get(CACHE_CONTROL);
	}

	public String getConnection(){
		return headerMap.get(CONNECTION);
	}

	public String getContentEncoding(){
		return headerMap.get(CONTENT_ENCODING);
	}

	public String getContentLanguage(){
		return headerMap.get(CONTENT_LANGUAGE);
	}

	public String getContentLength(){
		return headerMap.get(CONTENT_LENGTH);
	}

	public String getContentType(){
		return headerMap.get(CONTENT_TYPE);
	}

	public String getCookie(){
		return headerMap.get(COOKIE);
	}

	public String getDnt(){
		return headerMap.get(DNT);
	}

	public String getHost(){
		return headerMap.get(HOST);
	}

	public String getIfModifiedSince(){
		return headerMap.get(IF_MODIFIED_SINCE);
	}

	public String getOrigin(){
		return headerMap.get(ORIGIN);
	}

	public String getPragma(){
		return headerMap.get(PRAGMA);
	}

	public String getReferer(){
		return headerMap.get(REFERER);
	}

	public String getUserAgent(){
		return headerMap.get(USER_AGENT);
	}

	public String getXForwardedFor(){
		return headerMap.get(X_FORWARDED_FOR);
	}

	public String getXRequestedWith(){
		return headerMap.get(X_REQUESTED_WITH);
	}

	public String getOthers(){
		return headerMap.get("others");
	}

}
