package com.hotpads.util.http.client;

import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";
	
	private HttpUriRequest request;
	private Boolean retrySafe;
	private Map<String,String> headers;
	private Map<String,String> payload;
	
	public enum HttpMethod {
		DELETE, GET, PATCH, POST, PUT
	}
	
	public HotPadsHttpRequest(HttpMethod method, String url, boolean retrySafe) {
		if (method == HttpMethod.DELETE) {
			this.request = new HttpDelete(url);
			this.payload = Collections.emptyMap();
		} else if (method == HttpMethod.GET) {
			this.request = new HttpGet(url);
			this.payload = Collections.emptyMap();
		} else if (method == HttpMethod.PATCH) {
			this.request = new HttpPatch(url);
			this.payload = Collections.emptyMap();
		} else if (method == HttpMethod.POST) {
			this.request = new HttpPost(url);
			this.payload = new HashMap<>();
		} else if (method == HttpMethod.PUT) {
			this.request = new HttpPut(url);
			this.payload = new HashMap<>();
		}
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
	}
	
	private List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(Entry<String, String> entry : data.entrySet()){
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return params;
	}
	
	public HttpUriRequest getRequest() {
		return request;
	}
	
	public boolean getRetrySafe() {
		return retrySafe;
	}
	
	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public HttpEntity getEntity() {
		if(!(request instanceof HttpEntityEnclosingRequest)) {
			return null;
		}
		HttpEntityEnclosingRequest requestEntity = (HttpEntityEnclosingRequest) request;
		return requestEntity.getEntity();
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public HotPadsHttpRequest setEntity(String entity) {
		if (entity != null && request instanceof HttpEntityEnclosingRequest) {
			try {
				setEntity(new StringEntity(entity));
			} catch (UnsupportedEncodingException e) {
				throw new HotPadsHttpClientException(e);
			}
		}
		return this;
	}
	
	/**
	 * Entity exists only in HttpPut, HttpPatch, HttpPost
	 */
	public HotPadsHttpRequest setEntity(Map<String, String> entity) {
		if (entity != null && request instanceof HttpEntityEnclosingRequest) {
			try {
				setEntity(new UrlEncodedFormEntity(urlEncodeFromMap(entity)));
			}catch (UnsupportedEncodingException e){
				throw new HotPadsHttpClientException(e);
			}
		}
		return this;
	}
	
	private HotPadsHttpRequest setEntity(HttpEntity entity) {
		HttpEntityEnclosingRequest requestEntity = (HttpEntityEnclosingRequest) request;
		requestEntity.setEntity(entity);
		return this;
	}

	public Map<String,String> getHeaders() {
		return headers;
	}
	
	public HotPadsHttpRequest addHeaders(Map<String, String> headers) {
		if(headers != null) {
			for(Map.Entry<String,String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
		return this;
	}
	
	public HotPadsHttpRequest setContentType(String contentType) {
		if(contentType != null && !contentType.isEmpty()) {
			request.addHeader(CONTENT_TYPE, contentType);
		}
		return this;
	}
	
	/**
	 * Payload exists only in HttpPost, HttpPut
	 */
	public HotPadsHttpRequest addToPayload(String key, String value) {
		payload.put(key, value);
		return this;
	}
	
	/**
	 * Payload exists only in HttpPost, HttpPut
	 */
	public HotPadsHttpRequest addToPayload(HttpRequestConfig config) {
		return addToPayload(config.getParameterMap());
	}
	
	/**
	 * Payload exists only in HttpPost, HttpPut
	 */
	public HotPadsHttpRequest addToPayload(Map<String,String> payload) {
		this.payload.putAll(payload);
		return this;
	}

	/**
	 * Payload exists only in HttpPost and HttpPut
	 */
	public Map<String,String> getPayload() {
		return payload;
	}

}
