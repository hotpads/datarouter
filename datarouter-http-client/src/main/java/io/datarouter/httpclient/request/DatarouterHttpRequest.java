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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
	private boolean retrySafe;
	private Integer timeoutMs;
	private Long futureTimeoutMs;
	private HttpEntity entity;
	private String fragment;
	private Map<String,List<String>> headers;
	private Map<String,List<String>> queryParams;
	private Map<String,List<String>> postParams;
	private DatarouterHttpClientConfig config;
	private HttpHost proxy;
	private final List<BasicClientCookie> cookies;

	public enum HttpRequestMethod{
		DELETE, GET, HEAD, PATCH, POST, PUT
	}

	/**
	 * Expects query string parameters to already be UTF-8 encoded. See AdvancedStringTool.makeUrlParameters().
	 * URL fragment is stripped from URL when sent to server.
	 */
	public DatarouterHttpRequest(HttpRequestMethod method, final String url, boolean retrySafe){
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
		this.retrySafe = retrySafe;
		this.fragment = fragment;
		this.headers = new HashMap<>();
		this.queryParams = queryParams;
		this.postParams = new HashMap<>();
		this.cookies = new ArrayList<>();
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
			queryParams.computeIfAbsent(part, $ -> new ArrayList<>()).add(paramValue);
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
		if(timeoutMs != null || proxy != null){
			Builder builder = RequestConfig.custom();
			builder.setCookieSpec(CookieSpecs.STANDARD);
			if(timeoutMs != null){
				builder.setConnectTimeout(timeoutMs).setConnectionRequestTimeout(timeoutMs).setSocketTimeout(timeoutMs);
			}
			if(proxy != null){
				builder.setProxy(proxy);
			}

			RequestConfig requestConfig = builder.build();
			request.setConfig(requestConfig);
		}
		return request;
	}

	private HttpRequestBase getRequest(String url){
		switch(method){
		case DELETE:
			return new HttpDelete(url);
		case GET:
			return new HttpGet(url);
		case HEAD:
			return new HttpHead(url);
		case PATCH:
			return new HttpPatch(url);
		case POST:
			return new HttpPost(url);
		case PUT:
			return new HttpPut(url);
		default:
			throw new IllegalArgumentException("Invalid or null HttpMethod: " + method);
		}
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
		return getEntityAsString(null);
	}

	public String getEntityAsString(Charset charset){
		try{
			return EntityUtils.toString(entity, charset);
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
		headers.computeIfAbsent(name, $ -> new ArrayList<>()).add(value);
		return this;
	}

	public DatarouterHttpRequest addHeaders(Map<String,String> headers){
		return addEntriesToMap(this.headers, headers);
	}

	public DatarouterHttpRequest setContentType(ContentType contentType){
		if(contentType != null){
			headers.computeIfAbsent(CONTENT_TYPE, $ -> new ArrayList<>()).add(contentType.getMimeType());
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

	public DatarouterHttpRequest addPostParam(String name, String value){
		postParams.computeIfAbsent(name, $ -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * Post params are signed anded to the entity upon request execution.
	 */
	public DatarouterHttpRequest addPostParams(HttpRequestConfig config){
		return config == null ? this : addPostParams(config.getParameterMap());
	}

	/**
	 * Post params are signed anded to the entity upon request execution.
	 */
	public DatarouterHttpRequest addPostParams(Map<String,String> params){
		return addEntriesToMap(this.postParams, params);
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public boolean canHaveEntity(){
		return method == HttpRequestMethod.PATCH || method == HttpRequestMethod.POST || method == HttpRequestMethod.PUT;
	}

	/**
	 * This method expects parameters to not be URL encoded. Params are UTF-8 encoded upon request execution.
	 */
	public DatarouterHttpRequest addGetParam(String name, String value){
		queryParams.computeIfAbsent(name, $ -> new ArrayList<>()).add(value);
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

	private DatarouterHttpRequest addEntriesToMap(Map<String,List<String>> map, Map<String,String> entriesToAdd){
		if(entriesToAdd != null){
			for(Entry<String,String> entry : entriesToAdd.entrySet()){
				String key = entry.getKey();
				if(key == null || key.trim().isEmpty()){
					continue;
				}
				map.computeIfAbsent(key.trim(), $ -> new ArrayList<>()).add(entry.getValue());
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
		List<NameValuePair> params = new ArrayList<>();
		if(data != null && !data.isEmpty()){
			for(Entry<String,String> entry : data.entrySet()){
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		return params;
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
				.collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get(0)),
						HashMap::putAll);// don't use Collectors.toMap because values can be null
	}

	public boolean getRetrySafe(){
		return retrySafe;
	}

	public Integer getTimeoutMs(){
		return timeoutMs;
	}

	public DatarouterHttpRequest setTimeoutMs(Integer timeoutMs){
		this.timeoutMs = timeoutMs;
		return this;
	}

	public Long getFutureTimeoutMs(){
		return futureTimeoutMs;
	}

	public DatarouterHttpRequest setFutureTimeoutMs(Long futureTimeoutMs){
		this.futureTimeoutMs = futureTimeoutMs;
		return this;
	}

	public HttpHost getProxy(){
		return proxy;
	}

	public DatarouterHttpRequest setProxy(HttpHost proxy){
		this.proxy = proxy;
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

}
