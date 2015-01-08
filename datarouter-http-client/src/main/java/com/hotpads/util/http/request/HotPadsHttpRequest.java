package com.hotpads.util.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.apache.http.client.config.RequestConfig;
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
import org.apache.http.util.Args;

import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";

	private final HttpRequestMethod method;
	private final String path;
	private boolean retrySafe;
	private Integer timeoutMs;
	private Long futureTimeoutMs;
	private HttpEntity entity;
	private String fragment;
	private Map<String, String> headers;
	private Map<String, String> queryParams;
	private Map<String, String> postParams;

	public enum HttpRequestMethod {
		DELETE, GET, HEAD, PATCH, POST, PUT
	}

	/**
	 * Expects query string parameters to already be UTF-8 encoded. See AdvancedStringTool.makeUrlParameters().
	 * URL fragment is stripped from URL when sent to server.
	 */
	public HotPadsHttpRequest(HttpRequestMethod method, final String url, boolean retrySafe) {
		Args.notBlank(url, "request url");
		Args.notNull(method, "http method");

		String fragment;
		int fragmentIndex = url.indexOf('#');
		if (fragmentIndex > 0 && fragmentIndex < url.length() - 1) {
			fragment = url.substring(fragmentIndex + 1);
		} else {
			fragmentIndex = url.length();
			fragment = "";
		}
		String path = url.substring(0, fragmentIndex);

		Map<String, String> queryParams;
		int queryIndex = path.indexOf("?");
		if (queryIndex > 0) {
			queryParams = extractQueryParams(path.substring(queryIndex + 1));
			path = path.substring(0, queryIndex);
		} else {
			queryParams = new LinkedHashMap<>();
		}
		this.method = method;
		this.path = path;
		this.retrySafe = retrySafe;
		this.fragment = fragment;
		this.headers = new HashMap<>();
		this.queryParams = queryParams;
		this.postParams = new HashMap<>();
	}

	private Map<String, String> extractQueryParams(String queryString) {
		Map<String, String> queryParams = new LinkedHashMap<>();
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] parts = param.split("=", 2);
			String part = urlDecode(parts[0]);
			if (parts.length == 1) {
				queryParams.put(part, null);
			} else if (parts.length == 2) {
				queryParams.put(part, urlDecode(parts[1]));
			}
		}
		return queryParams;
	}

	public HttpRequestBase getRequest() {
		String url = getUrl();
		HttpRequestBase request = getRequest(method, url);
		if (!headers.isEmpty()) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
		if (entity != null && canHaveEntity()) {
			((HttpEntityEnclosingRequest) request).setEntity(entity);
		}
		if (timeoutMs != null) {
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeoutMs)
					.setConnectionRequestTimeout(timeoutMs).setSocketTimeout(timeoutMs).build();
			request.setConfig(requestConfig);
		}
		return request;
	}

	public String getUrl() {
		return path + (queryParams.isEmpty() ? "" : getQueryString());
	}

	public String getUrlFragment() {
		return fragment;
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
		return addEntriesToMap(this.headers, headers);
	}

	public HotPadsHttpRequest setContentType(ContentType contentType) {
		if (contentType != null) {
			headers.put(CONTENT_TYPE, contentType.getMimeType());
		}
		return this;
	}

	/** Post params are signed anded to the entity upon request execution. */
	public HotPadsHttpRequest addPostParams(HttpRequestConfig config) {
		return config == null ? this : addPostParams(config.getParameterMap());
	}

	/** Post params are signed anded to the entity upon request execution. */
	public HotPadsHttpRequest addPostParams(Map<String, String> params) {
		return addEntriesToMap(this.postParams, params);
	}

	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public boolean canHaveEntity() {
		return method == HttpRequestMethod.PATCH || method == HttpRequestMethod.POST || method == HttpRequestMethod.PUT;
	}

	/** This method expects parameters to not be URL encoded. Params are UTF-8 encoded upon request execution. */
	public HotPadsHttpRequest addGetParams(Map<String, String> params) {
		return addEntriesToMap(this.queryParams, params);
	}

	private HotPadsHttpRequest addEntriesToMap(Map<String, String> map, Map<String, String> entriesToAdd) {
		if (entriesToAdd != null) {
			for (Map.Entry<String, String> entry : entriesToAdd.entrySet()) {
				String key = entry.getKey();
				if (key == null || key.trim().isEmpty()) {
					continue;
				}
				map.put(key.trim(), entry.getValue());
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

	private String urlDecode(String encoded) {
		try {
			return encoded == null ? "" : URLDecoder.decode(encoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// unthinkable
			throw new RuntimeException("UTF-8 is unsupported", e);
		}
	}

	private String getQueryString() {
		StringBuilder query = new StringBuilder();
		for (Entry<String, String> param : queryParams.entrySet()) {
			String key = param.getKey();
			if (key == null || key.trim().isEmpty()) {
				continue;
			}
			query.append('&').append(urlEncode(key.trim()));
			String value = param.getValue();
			if (value != null && !value.isEmpty()) {
				query.append('=').append(urlEncode(value));
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

	public Map<String, String> getGetParams() {
		return queryParams;
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

	public Long getFutureTimeoutMs() {
		return futureTimeoutMs;
	}

	public HotPadsHttpRequest setFutureTimeoutMs(Long futureTimeoutMs) {
		this.futureTimeoutMs = futureTimeoutMs;
		return this;
	}

}
