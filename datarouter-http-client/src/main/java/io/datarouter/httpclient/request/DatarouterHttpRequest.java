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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

import io.datarouter.httpclient.client.DatarouterHttpClientConfig;

public class DatarouterHttpRequest{

	private static final String CONTENT_TYPE = "Content-Type";

	private final HttpRequestMethod method;
	private final String path;
	private final List<BasicClientCookie> cookies;

	private Boolean retrySafe;
	private Duration timeout;
	private HttpEntity entity;
	private final String fragment;
	private Map<String,List<String>> headers;
	private Map<String,List<String>> queryParams;
	private Map<String,List<String>> postParams;
	private DatarouterHttpClientConfig config;
	private HttpHost proxy;
	private Boolean shouldSkipSecurity;
	private Boolean shouldSkipLogs;
	private boolean disableFollowRedirects;
	private Duration logSlowRequestThreshold;

	/**
	 * Expects query string parameters to already be UTF-8 encoded. See AdvancedStringTool.makeUrlParameters().
	 * URL fragment is stripped from URL when sent to server.
	 */
	public DatarouterHttpRequest(HttpRequestMethod method, String url){
		this(method, url, false, false);
	}

	public DatarouterHttpRequest(
			HttpRequestMethod method,
			String url,
			boolean shouldSkipSecurity,
			boolean shouldSkipLogs){
		Args.notBlank(url, "request url");
		Args.notNull(method, "http method");

		String fragment;
		int fragmentIndex = url.indexOf('#');
		if(fragmentIndex > 0 && fragmentIndex < url.length() - 1){
			fragment = url.substring(fragmentIndex + 1);
		}else{
			fragmentIndex = url.length();
			fragment = "";
		}
		String path = url.substring(0, fragmentIndex);

		Map<String,List<String>> queryParams;
		int queryIndex = path.indexOf("?");
		if(queryIndex > 0){
			queryParams = extractQueryParams(path.substring(queryIndex + 1));
			path = path.substring(0, queryIndex);
		}else{
			queryParams = new LinkedHashMap<>();
		}
		this.method = method;
		this.path = path;
		this.fragment = fragment;
		this.headers = new HashMap<>();
		this.queryParams = queryParams;
		this.postParams = new HashMap<>();
		this.cookies = new ArrayList<>();
		this.shouldSkipSecurity = shouldSkipSecurity;
		this.shouldSkipLogs = shouldSkipLogs;
	}

	private Map<String,List<String>> extractQueryParams(String queryString){
		Map<String,List<String>> queryParams = new LinkedHashMap<>();
		String[] params = queryString.split("&");
		for(String param : params){
			String[] parts = param.split("=", 2);
			String part = urlDecode(parts[0]);
			String paramValue = null;
			if(parts.length == 2){
				paramValue = urlDecode(parts[1]);
			}
			queryParams.computeIfAbsent(part, _ -> new ArrayList<>()).add(paramValue);
		}
		return queryParams;
	}

	public HttpRequestBase getRequest(){
		String url = getUrl();
		HttpRequestBase request = getRequest(url);
		if(!headers.isEmpty()){
			for(Entry<String,List<String>> header : headers.entrySet()){
				for(String headerValue : header.getValue()){
					request.addHeader(header.getKey(), headerValue);
				}
			}
		}
		if(entity != null && canHaveEntity()){
			((HttpEntityEnclosingRequest) request).setEntity(entity);
		}
		if(timeout != null || proxy != null || disableFollowRedirects){
			Builder builder = RequestConfig.custom();
			builder.setCookieSpec(CookieSpecs.STANDARD);
			if(timeout != null){
				int requestTimeout = (int)timeout.toMillis();
				builder
						.setConnectTimeout(requestTimeout)
						.setConnectionRequestTimeout(requestTimeout)
						.setSocketTimeout(requestTimeout);
			}
			if(proxy != null){
				builder.setProxy(proxy);
			}
			if(disableFollowRedirects){
				builder.setRedirectsEnabled(false);
			}

			RequestConfig requestConfig = builder.build();
			request.setConfig(requestConfig);
		}
		return request;
	}

	private HttpRequestBase getRequest(String url){
		return method.httpRequestBase.apply(url);
	}

	public String getUrl(){
		return path + (queryParams.isEmpty() ? "" : getQueryString());
	}

	public HttpRequestMethod getMethod(){
		return method;
	}

	public String getUrlFragment(){
		return fragment;
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public HttpEntity getEntity(){
		return entity;
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public String getEntityAsString(){
		try{
			if(entity == null){
				return null;
			}
			return EntityUtils.toString(entity);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public DatarouterHttpRequest setEntity(String entity, ContentType contentType){
		this.entity = new StringEntity(entity, contentType);
		this.setContentType(contentType);
		return this;
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public DatarouterHttpRequest setEntity(Map<String,String> entity){
		this.entity = new UrlEncodedFormEntity(urlEncodeFromMap(entity), StandardCharsets.UTF_8);
		return this;
	}

	public DatarouterHttpRequest setEntity(HttpEntity httpEntity){
		this.entity = httpEntity;
		return this;
	}

	public DatarouterHttpRequest addHeader(String name, String value){
		headers.computeIfAbsent(name, _ -> new ArrayList<>()).add(value);
		return this;
	}

	public DatarouterHttpRequest addHeaders(Map<String,String> headers){
		return addEntriesToMap(this.headers, headers);
	}

	public DatarouterHttpRequest setHeaders(Map<String,List<String>> headers){
		this.headers = headers;
		return this;
	}

	public DatarouterHttpRequest setContentType(ContentType contentType){
		if(contentType != null){
			List<String> oldHeaders = Optional.ofNullable(headers.get(CONTENT_TYPE)).orElseGet(List::of);
			List<String> newHeaders = new ArrayList<>(oldHeaders);
			newHeaders.add(contentType.getMimeType());
			headers.put(CONTENT_TYPE, newHeaders);
		}
		return this;
	}

	public DatarouterHttpRequest addParam(String name, Object value){
		Objects.requireNonNull(value);
		if(HttpRequestMethod.GET == method){
			addGetParam(name, value.toString());
		}else if(HttpRequestMethod.POST == method){
			addPostParam(name, value.toString());
		}else{
			throw new IllegalArgumentException("Only GET and POST methods supported");
		}
		return this;
	}

	/*
	 * values in map cannot be null
	 */
	public DatarouterHttpRequest addParams(Map<String,String> params){
		params.forEach(this::addParam);
		return this;
	}

	public DatarouterHttpRequest addPostParam(String name, String value){
		postParams.computeIfAbsent(name, _ -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * Post params are signed and added to the entity upon request execution.
	 */
	public DatarouterHttpRequest addPostParams(HttpRequestConfig config){
		return config == null ? this : addPostParams(config.getParameterMap());
	}

	/**
	 * Post params are signed and added to the entity upon request execution.
	 */
	public DatarouterHttpRequest addPostParams(Map<String,String> params){
		return addEntriesToMap(this.postParams, params);
	}

	public DatarouterHttpRequest setPostParams(Map<String,List<String>> params){
		this.postParams = params;
		return this;
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost, DatarouterHttpDeleteRequestWithEntity
	 */
	public boolean canHaveEntity(){
		return method.allowEntity;
	}

	/**
	 * This method expects parameters to not be URL encoded. Params are UTF-8 encoded upon request execution.
	 */
	public DatarouterHttpRequest addGetParam(String name, String value){
		queryParams.computeIfAbsent(name, _ -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * This method expects parameters to not be URL encoded. Params are UTF-8 encoded upon request execution.
	 */
	public DatarouterHttpRequest addGetParams(Map<String,String> params){
		return addEntriesToMap(this.queryParams, params);
	}

	public DatarouterHttpRequest addGetParams(HttpRequestConfig config){
		return config == null ? this : addGetParams(config.getParameterMap());
	}

	public DatarouterHttpRequest setGetParams(Map<String,List<String>> params){
		this.queryParams = params;
		return this;
	}

	private DatarouterHttpRequest addEntriesToMap(Map<String,List<String>> map, Map<String,String> entriesToAdd){
		if(entriesToAdd != null){
			for(Entry<String,String> entry : entriesToAdd.entrySet()){
				String key = entry.getKey();
				if(key == null || key.trim().isEmpty()){
					continue;
				}
				map.computeIfAbsent(key.trim(), _ -> new ArrayList<>()).add(entry.getValue());
			}
		}
		return this;
	}

	public DatarouterHttpRequest addBasicAuthorizationHeaders(String username, String password){
		String encodedCredentials = Base64.encodeBase64String((username + ":" + password).getBytes());
		addHeader("Authorization", "Basic " + encodedCredentials);
		return this;
	}

	public DatarouterHttpRequest addBearerAuthorizationHeader(String accessToken){
		addHeader("Authorization", "Bearer " + accessToken);
		return this;
	}

	// from AdvancedStringTool
	private String urlEncode(String unencoded){
		try{
			return unencoded == null ? "" : URLEncoder.encode(unencoded, "UTF-8");
		}catch(UnsupportedEncodingException e){
			// unthinkable
			throw new RuntimeException("UTF-8 is unsupported", e);
		}
	}

	private String urlDecode(String encoded){
		try{
			return encoded == null ? "" : URLDecoder.decode(encoded, "UTF-8");
		}catch(UnsupportedEncodingException e){
			// unthinkable
			throw new RuntimeException("UTF-8 is unsupported", e);
		}
	}

	private String getQueryString(){
		StringBuilder query = new StringBuilder();
		for(Entry<String,List<String>> param : queryParams.entrySet()){
			String key = param.getKey();
			if(key == null || key.trim().isEmpty()){
				continue;
			}
			String urlEncodedKey = urlEncode(key.trim());
			for(String value : param.getValue()){
				query.append('&').append(urlEncodedKey);
				if(value != null && !value.isEmpty()){
					query.append('=').append(urlEncode(value));
				}
			}
		}
		return "?" + query.substring(1);
	}

	private List<NameValuePair> urlEncodeFromMap(Map<String,String> data){
		if(data == null || data.isEmpty()){
			return List.of();
		}
		return data.entrySet().stream()
				.map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
				.collect(Collectors.toUnmodifiableList());
	}

	public DatarouterHttpClientConfig getRequestConfig(DatarouterHttpClientConfig clientConfig){
		if(config != null){
			return config;
		}
		return clientConfig;
	}

	public DatarouterHttpRequest overrideConfig(DatarouterHttpClientConfig config){
		this.config = config;
		return this;
	}

	public Map<String,List<String>> getHeaders(){
		return headers;
	}

	public Map<String,List<String>> getGetParams(){
		return queryParams;
	}

	public Map<String,List<String>> getPostParams(){
		return postParams;
	}

	public Map<String,String> getFirstHeaders(){
		return extractFirstElementOfValueList(headers);
	}

	public Map<String,String> getFirstGetParams(){
		return extractFirstElementOfValueList(queryParams);
	}

	public Map<String,String> getFirstPostParams(){
		return extractFirstElementOfValueList(postParams);
	}

	private static Map<String,String> extractFirstElementOfValueList(Map<String,List<String>> mapOfLists){
		return mapOfLists.entrySet().stream()
				.collect(HashMap::new,
						(map, entry) -> map.put(entry.getKey(),
						entry.getValue().getFirst()),
						HashMap::putAll);// don't use Collectors.toMap because values can be null
	}

	public boolean getRetrySafe(){
		if(retrySafe == null){
			return getMethod().defaultRetrySafe;
		}
		return retrySafe;
	}

	public DatarouterHttpRequest setRetrySafe(boolean retrySafe){
		this.retrySafe = retrySafe;
		return this;
	}

	public Duration getTimeout(){
		return timeout;
	}

	public DatarouterHttpRequest setTimeout(Duration timeout){
		this.timeout = timeout;
		return this;
	}

	public HttpHost getProxy(){
		return proxy;
	}

	public DatarouterHttpRequest setProxy(HttpHost proxy){
		this.proxy = proxy;
		return this;
	}

	public DatarouterHttpRequest disableFollowRedirects(){
		disableFollowRedirects = true;
		return this;
	}

	public DatarouterHttpRequest addCookie(BasicClientCookie cookie){
		cookies.add(cookie);
		return this;
	}

	public List<BasicClientCookie> getCookies(){
		return cookies;
	}

	public String getPath(){
		return path;
	}

	public String getInitialUrl(){
		return getPath();
	}

	public boolean getShouldSkipSecurity(){
		return shouldSkipSecurity;
	}

	public DatarouterHttpRequest setShouldSkipSecurity(boolean shouldSkipSecurity){
		this.shouldSkipSecurity = shouldSkipSecurity;
		return this;
	}

	public boolean getShouldSkipLogs(){
		return shouldSkipLogs;
	}

	public DatarouterHttpRequest setShouldSkipLogs(boolean shouldSkipLogs){
		this.shouldSkipLogs = shouldSkipLogs;
		return this;
	}

	public Optional<Duration> findLogSlowRequestThreshold(){
		return Optional.ofNullable(logSlowRequestThreshold);
	}

	public DatarouterHttpRequest setLogSlowRequestThreshold(Duration logSlowRequestThreshold){
		this.logSlowRequestThreshold = logSlowRequestThreshold;
		return this;
	}

}
