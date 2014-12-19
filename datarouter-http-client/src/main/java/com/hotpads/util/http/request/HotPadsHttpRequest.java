package com.hotpads.util.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";

	private final HttpRequestMethod method;
	private final String path;
	private boolean retrySafe;
	private Integer timeoutMs;
	private HttpEntity entity;
	private Map<String, String> headers;
	private Map<String, String> queryParams;
	private Map<String, String> postParams;

	public enum HttpRequestMethod {
		DELETE, GET, HEAD, PATCH, POST, PUT
	}

	public HotPadsHttpRequest(HttpRequestMethod method, String url, boolean retrySafe) {
		int queryIndex = url.indexOf("?");
		String path;
		Map<String, String> queryParams;
		if (queryIndex > 0) {
			path = url.substring(0, queryIndex);
			queryParams = extractQueryParams(url.substring(queryIndex + 1));
		} else {
			path = url;
			queryParams = new LinkedHashMap<>();
		}
		this.method = method;
		this.path = path;
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
		this.queryParams = queryParams;
		this.postParams = new HashMap<>();
	}

	public Map<String, String> extractQueryParams(String queryString) {
		Map<String, String> queryParams = new LinkedHashMap<>();
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] parts = param.split("=");
			if (parts.length == 1) {
				queryParams.put(parts[0], null);
			} else if (parts.length == 2) {
				queryParams.put(parts[0], parts[1]);
			}
		}
		return queryParams;
	}

	public HttpRequestBase getRequest() {
		String url = path + (queryParams.isEmpty() ? "" : getQueryString());
		HttpRequestBase request = getRequest(method, url);
		if (!headers.isEmpty()) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
		if (entity != null && canHaveEntity()) {
			((HttpEntityEnclosingRequest) request).setEntity(entity);
		}
		return request;
	}

	private HttpRequestBase getRequest(HttpRequestMethod method, String url) {
		switch (method) {
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

	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public HttpEntity getEntity() {
		return entity;
	}

	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(String entity) {
		try {
			this.entity = new StringEntity(entity);
		} catch (UnsupportedEncodingException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
		return this;
	}

	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(Map<String, String> entity) {
		try {
			this.entity = new UrlEncodedFormEntity(urlEncodeFromMap(entity));
		} catch (UnsupportedEncodingException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
		return this;
	}

	public HotPadsHttpRequest addHeaders(Map<String, String> headers) {
		return addEntriesToMap(this.headers, headers, false);
	}

	public HotPadsHttpRequest setContentType(ContentType contentType) {
		if (contentType != null) {
			headers.put(CONTENT_TYPE, contentType.getMimeType());
		}
		return this;
	}

	public HotPadsHttpRequest addPostParams(HttpRequestConfig config) {
		return config == null ? this : addPostParams(config.getParameterMap());
	}

	public HotPadsHttpRequest addPostParams(Map<String, String> params) {
		return addEntriesToMap(this.postParams, params, false);
	}

	public boolean canHaveEntity() {
		return method == HttpRequestMethod.PATCH || method == HttpRequestMethod.POST || method == HttpRequestMethod.PUT;
	}

	public HotPadsHttpRequest addGetParams(Map<String, String> params) {
		return addEntriesToMap(this.queryParams, params, true);
	}
	
	private HotPadsHttpRequest addEntriesToMap(Map<String, String> map, Map<String, String> entriesToAdd,
			boolean urlEncode) {
		if (entriesToAdd != null) {
			for (Map.Entry<String, String> entry : entriesToAdd.entrySet()) {
				String key = entry.getKey();
				if (key == null || key.trim().isEmpty()) {
					continue;
				}
				if(urlEncode) {
					map.put(urlEncode(key.trim()), urlEncode(entry.getValue()));
				} else {
					map.put(key.trim(), entry.getValue());
				}
			}
		}
		return this;
	}

	// from AdvancedStringTool
	private String urlEncode(String unencoded) {
		try {
			return unencoded == null ? "" : URLEncoder.encode(unencoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// unthinkable
			throw new RuntimeException("UTF-8 is unsupported", e);
		}
	}

	private String getQueryString() {
		StringBuilder query = new StringBuilder();
		for (Entry<String, String> param : queryParams.entrySet()) {
			query.append('&').append(param.getKey());
			String value = param.getValue();
			if (value != null && !value.isEmpty()) {
				query.append('=').append(param.getValue());
			}
		}
		return "?" + query.substring(1);
	}

	private List<NameValuePair> urlEncodeFromMap(Map<String, String> data) {
		List<NameValuePair> params = new ArrayList<>();
		if (data != null && !data.isEmpty()) {
			for (Entry<String, String> entry : data.entrySet()) {
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		return params;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getPostParams() {
		return postParams;
	}

	public boolean getRetrySafe() {
		return retrySafe;
	}

	public Integer getTimeoutMs() {
		return timeoutMs;
	}

	public HotPadsHttpRequest setTimeoutMs(Integer timeoutMs) {
		this.timeoutMs = timeoutMs;
		return this;
	}
}
