package com.hotpads.util.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";

	private final HttpRequestMethod method;
	private final String path;
	private String queryString;
	private boolean retrySafe;
	private Integer timeoutMs;
	private HttpEntity entity;
	private Map<String, String> headers;
	private Map<String, String> postParams;

	public enum HttpRequestMethod {
		DELETE, GET, HEAD, PATCH, POST, PUT
	}

	public HotPadsHttpRequest(HttpRequestMethod method, String url, boolean retrySafe) {
		int queryIndex = url.indexOf("?");
		String path, query;
		if (queryIndex > 0) {
			path = url.substring(0, queryIndex);
			query = url.substring(queryIndex + 1);
		} else {
			path = url;
			query = "";
		}
		this.method = method;
		this.path = path;
		this.queryString = query;
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
		this.postParams = new HashMap<>();
	}

	public HttpRequestBase getRequest() {
		String url = path + (queryString.isEmpty() ? "" : "?" + queryString);
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
			throw new IllegalArgumentException("invalid or null HttpMethod: " + method);
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
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				headers.put(header.getKey(), header.getValue());
			}
		}
		return this;
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
		if (params != null && !params.isEmpty()) {
			this.postParams.putAll(params);
		}
		return this;
	}

	public boolean canHaveEntity() {
		return method == HttpRequestMethod.PATCH || method == HttpRequestMethod.POST || method == HttpRequestMethod.PUT;
	}

	public HotPadsHttpRequest addGetParams(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return this;
		}
		StringBuilder query = new StringBuilder(queryString);
		for (Entry<String, String> param : params.entrySet()) {
			String key = param.getKey();
			if (key == null || key.trim().isEmpty()) {
				continue;
			}
			query.append('&').append(urlEncode(key.trim()));
			String value = param.getValue();
			if (value != null && !value.isEmpty()) {
				query.append('=').append(urlEncode(param.getValue()));
			}
		}
		this.queryString = query.substring(queryString.isEmpty() ? 1 : 0);
		return this;
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

	// from AdvancedStringTool
	private String urlEncode(String unencoded) {
		try {
			return unencoded == null ? "" : URLEncoder.encode(unencoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// unthinkable
			throw new RuntimeException("UTF-8 is unsupported", e);
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getQueryString() {
		return queryString;
	}

	public Map<String, String> getPostParams() {
		return postParams;
	}

	public boolean getRetrySafe() {
		return retrySafe;
	}

	public void setRetrySafe(boolean retrySafe) {
		this.retrySafe = retrySafe;
	}

	public Integer getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(Integer timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
}
