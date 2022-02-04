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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.httpclient.HttpHeaders;

public class RecordedHttpHeaders{

	private static final String[] RECORDED_HEADERS = {
			HttpHeaders.ACCEPT_CHARSET,
			HttpHeaders.ACCEPT_ENCODING,
			HttpHeaders.ACCEPT_LANGUAGE,
			HttpHeaders.ACCEPT,
			HttpHeaders.CACHE_CONTROL,
			HttpHeaders.CONNECTION,
			HttpHeaders.CONTENT_ENCODING,
			HttpHeaders.CONTENT_LANGUAGE,
			HttpHeaders.CONTENT_LENGTH,
			HttpHeaders.CONTENT_TYPE,
			HttpHeaders.COOKIE,
			HttpHeaders.DNT,
			HttpHeaders.HOST,
			HttpHeaders.IF_MODIFIED_SINCE,
			HttpHeaders.ORIGIN,
			HttpHeaders.PRAGMA,
			HttpHeaders.REFERER,
			HttpHeaders.USER_AGENT,
			HttpHeaders.X_FORWARDED_FOR,
			HttpHeaders.X_REQUESTED_WITH,
	};

	private final Map<String,String> headerMap = new HashMap<>();

	public RecordedHttpHeaders(HttpServletRequest request){
		if(request == null){
			return;
		}
		List<String> tmpHeaders = Collections.list(request.getHeaderNames());
		for(String headerKey : RECORDED_HEADERS){
			if(tmpHeaders.remove(headerKey)){
				headerMap.put(headerKey, String.join(", ", Collections.list(request.getHeaders(headerKey))));
			}
		}
		Map<String,List<String>> others = tmpHeaders.stream()
				.collect(Collectors.toMap(Function.identity(), name -> Collections.list(request.getHeaders(name))));
		headerMap.put("others", GsonTool.GSON.toJson(others));
	}


	public RecordedHttpHeaders(SortedMap<String,List<String>> sortedHeaders){
		if(sortedHeaders == null){
			return;
		}
		List<String> tmpHeaders = new ArrayList<>(sortedHeaders.keySet());
		for(String headerKey : RECORDED_HEADERS){
			if(tmpHeaders.remove(headerKey)){
				this.headerMap.put(headerKey, String.join(", ", sortedHeaders.get(headerKey)));
			}
		}
		Map<String,Collection<String>> others = tmpHeaders.stream()
				.collect(Collectors.toMap(Function.identity(), sortedHeaders::get));
		this.headerMap.put("others", GsonTool.GSON.toJson(others));
	}

	public String getAcceptCharset(){
		return headerMap.get(HttpHeaders.ACCEPT_CHARSET);
	}

	public String getAcceptEncoding(){
		return headerMap.get(HttpHeaders.ACCEPT_ENCODING);
	}

	public String getAcceptLanguage(){
		return headerMap.get(HttpHeaders.ACCEPT_LANGUAGE);
	}

	public String getAccept(){
		return headerMap.get(HttpHeaders.ACCEPT);
	}

	public String getCacheControl(){
		return headerMap.get(HttpHeaders.CACHE_CONTROL);
	}

	public String getConnection(){
		return headerMap.get(HttpHeaders.CONNECTION);
	}

	public String getContentEncoding(){
		return headerMap.get(HttpHeaders.CONTENT_ENCODING);
	}

	public String getContentLanguage(){
		return headerMap.get(HttpHeaders.CONTENT_LANGUAGE);
	}

	public String getContentLength(){
		return headerMap.get(HttpHeaders.CONTENT_LENGTH);
	}

	public String getContentType(){
		return headerMap.get(HttpHeaders.CONTENT_TYPE);
	}

	public String getCookie(){
		return headerMap.get(HttpHeaders.COOKIE);
	}

	public String getDnt(){
		return headerMap.get(HttpHeaders.DNT);
	}

	public String getHost(){
		return headerMap.get(HttpHeaders.HOST);
	}

	public String getIfModifiedSince(){
		return headerMap.get(HttpHeaders.IF_MODIFIED_SINCE);
	}

	public String getOrigin(){
		return headerMap.get(HttpHeaders.ORIGIN);
	}

	public String getPragma(){
		return headerMap.get(HttpHeaders.PRAGMA);
	}

	public String getReferer(){
		return headerMap.get(HttpHeaders.REFERER);
	}

	public String getUserAgent(){
		return headerMap.get(HttpHeaders.USER_AGENT);
	}

	public String getXForwardedFor(){
		return headerMap.get(HttpHeaders.X_FORWARDED_FOR);
	}

	public String getXRequestedWith(){
		return headerMap.get(HttpHeaders.X_REQUESTED_WITH);
	}

	public String getOthers(){
		return headerMap.get("others");
	}

}
