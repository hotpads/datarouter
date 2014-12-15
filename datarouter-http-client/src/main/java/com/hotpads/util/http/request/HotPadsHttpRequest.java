package com.hotpads.util.http.request;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
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
import org.apache.http.message.BasicNameValuePair;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";

	private final HttpMethod method;
	private final boolean retrySafe;
	private URL url;
	private Map<String,String> headers;
	private Map<String,String> params;
	private HttpEntity entity;
	private Integer timeoutMs;
	private String queryString;
	
	public HotPadsHttpRequest(HttpMethod method, String url, boolean retrySafe) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			IllegalArgumentException ex = new IllegalArgumentException("invalid url: " + url);
			ex.initCause(e);
			throw ex;
		}
		this.queryString = this.url.getQuery();
		this.headers = new HashMap<>();
		this.params = new HashMap<>();
		this.method = method;
		this.retrySafe = retrySafe;
	}
	
	public HttpRequestBase getRequest() {
		HttpRequestBase request = null;
		String urlString = url.toString();
		if (method == HttpMethod.DELETE) {
			request = new HttpDelete(urlString);
		} else if (method == HttpMethod.GET) {
			request = new HttpGet(urlString);
		} else if (method == HttpMethod.PATCH) {
			request = new HttpPatch(urlString);
		} else if (method == HttpMethod.POST) {
			request = new HttpPost(urlString);
		} else if (method == HttpMethod.PUT) {
			request = new HttpPut(urlString);
		} else if (method == HttpMethod.HEAD) {
			request = new HttpHead(urlString);
		}
		if(request instanceof HttpEntityEnclosingRequest && entity != null) {
			HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
			entityRequest.setEntity(entity);
		}
		return request;
	}
	
	public boolean getRetrySafe() {
		return retrySafe;
	}
	
	/** Entity only in HttpPut, HttpPatch, HttpPost */
	public HttpEntity getEntity() {
		return entity;
	}

	/** Entity only in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(String entity) {
		try {
			this.entity = new StringEntity(entity);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}
	
	/** Entity only in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(Map<String, String> entity) {
		try {
			this.entity = new UrlEncodedFormEntity(urlEncodeFromMap(entity));
		}catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	public Map<String,String> getHeaders() {
		return headers;
	}
	
	public HotPadsHttpRequest addHeaders(Map<String, String> headers) {
		headers.putAll(headers);
		return this;
	}
	
	public HotPadsHttpRequest setContentType(ContentType contentType) {
		if(contentType != null) {
			addHeaders(Collections.singletonMap(CONTENT_TYPE, contentType.getMimeType()));
		}
		return this;
	}

	public HotPadsHttpRequest addGetParams(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return this;
		}
		StringBuilder queryString = new StringBuilder();
		for (Entry<String, String> param : params.entrySet()) {
			if(param == null) {
				continue;
			}
			String key = param.getKey();
			if (key == null || key.trim().isEmpty()) {
				continue;
			}
			queryString.append('&').append(key.trim()).append('=').append(param.getValue());
		}
		String query = url.getQuery();
		url ? queryString.toString() : '?' + queryString.substring(1);
		hasQueryString = true;
		return this;
	}

	public HotPadsHttpRequest addPostParams(HttpRequestConfig config) {
		return config == null ? this : addPostParams(config.getParameterMap());
	}
	
	public HotPadsHttpRequest addPostParams(Map<String,String> params) {
		if(params != null && !params.isEmpty()) {
			this.params.putAll(params);
		}
		return this;
	}
	
	public Map<String,String> getPostParams() {
		return params == null ? Collections.<String,String>emptyMap() : params;
	}
	
	private List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
		List<NameValuePair> params = new ArrayList<>();
		if(data != null && !data.isEmpty()) {
			for(Entry<String, String> entry : data.entrySet()){
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		return params;
	}

	public Integer getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(Integer timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
}
