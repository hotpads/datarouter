package com.hotpads.util.http.request;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
	
	private final HttpRequestBase request;
	private String queryString;
	private boolean retrySafe;
	private Integer timeoutMs;
	private Map<String,String> headers;
	private Map<String,String> postParams;
	
	public enum HttpRequestMethod {
		DELETE, GET, HEAD, PATCH, POST, PUT
	}
	
	public HotPadsHttpRequest(HttpRequestMethod method, String url, boolean retrySafe) {
		String path, query;
		try {
			URL urlObj = new URL(url);
			path = urlObj.getPath();
			query = urlObj.getQuery();
		} catch (MalformedURLException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
		this.request = setRequest(method, path);
		this.queryString = query;
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
		this.postParams = new HashMap<>();
	}
	
	public HttpRequestBase getRequest() {
		if(queryString.length() != 0) {
			try {
				request.setURI(new URI(request.getURI().toString() + queryString));
			} catch (URISyntaxException e) {}
		}
		return request;
	}
	
	private HttpRequestBase setRequest(HttpRequestMethod method, String url) {
		switch(method) {
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
		if(!canHaveEntity()) {
			return null;
		}
		HttpEntityEnclosingRequest requestEntity = (HttpEntityEnclosingRequest) request;
		return requestEntity.getEntity();
	}

	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(String entity) {
		try {
			setEntity(new StringEntity(entity));
		} catch (UnsupportedEncodingException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
		return this;
	}
	
	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	public HotPadsHttpRequest setEntity(Map<String, String> entity) {
		try {
			setEntity(new UrlEncodedFormEntity(urlEncodeFromMap(entity)));
		}catch (UnsupportedEncodingException e){
			throw new HotPadsHttpRuntimeException(e);
		}
		return this;
	}
	
	public HotPadsHttpRequest setEntity(HttpEntity entity) {
		if(entity != null && canHaveEntity()) {
			HttpEntityEnclosingRequest requestEntity = (HttpEntityEnclosingRequest) request;
			requestEntity.setEntity(entity);
		}
		return this;
	}
	
	public HotPadsHttpRequest addHeaders(Map<String, String> headers) {
		if(headers != null) {
			for(Map.Entry<String,String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
		return this;
	}
	
	public HotPadsHttpRequest setContentType(ContentType contentType) {
		if(contentType != null) {
			request.addHeader(CONTENT_TYPE, contentType.getMimeType());
		}
		return this;
	}
	
	public HotPadsHttpRequest addPostParams(HttpRequestConfig config) {
		return config == null ? this : addPostParams(config.getParameterMap());
	}
	
	public HotPadsHttpRequest addPostParams(Map<String,String> params) {
		if(params != null && !params.isEmpty()) {
			this.postParams.putAll(params);
		}
		return this;
	}

	public boolean canHaveEntity() {
		return this.request instanceof HttpEntityEnclosingRequest;
	}

	public HotPadsHttpRequest addGetParams(Map<String,String> params) {
		if(params == null || params.isEmpty()) {
			return this;
		}
		StringBuilder query = new StringBuilder(queryString);
		for(Entry<String,String> param : params.entrySet()) {
			String key = param.getKey();
			if(key == null || key.trim().isEmpty()) {
				continue;
			}
			query.append('&').append(key.trim()).append('=').append(param.getValue());
		}
		this.queryString = query.length() == 0 ? query.toString() : '?' + query.substring(1);
		return this;
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
	
	// from AdvancedStringTool
	private String urlEncode(String unencoded){
		try {
			return unencoded == null ? "" : URLEncoder.encode(unencoded,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			//unthinkable
			throw new RuntimeException("UTF-8 is unsupported",e);
		}
	}

	public Map<String,String> getHeaders() {
		return headers;
	}
	
	public String getQueryString() {
		return queryString;
	}

	public Map<String,String> getPostParams() {
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
