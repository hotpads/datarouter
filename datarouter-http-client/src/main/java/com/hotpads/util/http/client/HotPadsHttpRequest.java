package com.hotpads.util.http.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

public class HotPadsHttpRequest {

	private HttpUriRequest request;
	private Boolean retrySafe;
	private String contentType;
	private Map<String,String> headers;
	private Map<String,String> payload; // supported only for POST and PUT
	
	public enum HttpMethod {
		GET, POST, PUT, DELETE, PATCH
	}
	
	public HotPadsHttpRequest(HttpMethod method, String url, boolean retrySafe) {
		switch (method) {
		case GET:
			this.request = new HttpGet(url);
			break;
		case POST:
			this.request = new HttpPost(url);
			break;
		case PATCH:
			this.request = new HttpPatch(url);
			break;
		case PUT:
			this.request = new HttpPut(url);
			break;
		case DELETE:
			this.request = new HttpDelete(url);
			break;
		}
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
	}
	
	private static List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
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

	public void setRetrySafe(Boolean retrySafe) {
		this.retrySafe = retrySafe;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public HttpEntity getEntity() {
		if(!(request instanceof HttpEntityEnclosingRequestBase)) {
			return null;
		}
		HttpEntityEnclosingRequestBase requestEntity = (HttpEntityEnclosingRequestBase) request;
		return requestEntity.getEntity();
	}

	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public void setEntity(String entity) {
		if (entity == null || !(request instanceof HttpEntityEnclosingRequestBase)) {
			return;
		}
		try {
			setEntity(new StringEntity(entity));
		} catch (UnsupportedEncodingException e) {
			throw new HotPadsHttpClientException(e);
		}
	}
	
	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost
	 */
	public void setEntity(Map<String, String> entity) {
		if (entity == null || !(request instanceof HttpEntityEnclosingRequestBase)) {
			return;
		}
		try {
			setEntity(new UrlEncodedFormEntity(urlEncodeFromMap(entity)));
		}catch (UnsupportedEncodingException e){
			throw new HotPadsHttpClientException(e);
		}
	}
	
	/**
	 * Entities only exist in HttpPut, HttpPatch, HttpPost, HttpGetWithEntity
	 * @param entity
	 */
	public void setEntity(HttpEntity entity) {
		if(entity == null || !(request instanceof HttpEntityEnclosingRequestBase)) {
			return;
		}
		HttpEntityEnclosingRequestBase requestEntity = (HttpEntityEnclosingRequestBase) request;
		requestEntity.setEntity(entity);
	}

	public Map<String,String> getHeaders() {
		return headers;
	}
	
	public HotPadsHttpRequest setHeaders(Map<String, String> headers) {
		if(headers != null) {
			for(Map.Entry<String,String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
		return this;
	}
	
	public HotPadsHttpRequest setPayload(Map<String,String> payload) {
		this.payload = payload;
		return this;
	}

	public Map<String,String> getPayload() {
		return payload;
	}

}
