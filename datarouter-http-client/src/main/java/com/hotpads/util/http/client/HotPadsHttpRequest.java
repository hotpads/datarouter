package com.hotpads.util.http.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

public class HotPadsHttpRequest {

	private static final String CONTENT_TYPE = "Content-Type";
	
	private HttpRequestBase request;
	private Boolean retrySafe;
	private Map<String,String> headers;
	private Map<String,String> params;
	private boolean hasQueryString;
	
	public enum HttpMethod {
		DELETE, GET, PATCH, POST, PUT
	}
	
	public HotPadsHttpRequest(HttpMethod method, String url, boolean retrySafe) {
		if (method == HttpMethod.DELETE) {
			this.request = new HttpDelete(url);
		} else if (method == HttpMethod.GET) {
			this.request = new HttpGet(url);
		} else if (method == HttpMethod.PATCH) {
			this.request = new HttpPatch(url);
		} else if (method == HttpMethod.POST) {
			this.request = new HttpPost(url);
		} else if (method == HttpMethod.PUT) {
			this.request = new HttpPut(url);
		}
		this.retrySafe = retrySafe;
		this.headers = new HashMap<>();
		this.params = new HashMap<>();
		this.hasQueryString = false;
	}
	
	public HttpRequestBase getRequest() {
		return request;
	}
	
	public boolean getRetrySafe() {
		return retrySafe;
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
		if (entity != null && canHaveEntity()) {
			try {
				setEntity(new StringEntity(entity));
			} catch (UnsupportedEncodingException e) {
				throw new HotPadsHttpClientException(e);
			}
		}
		return this;
	}
	
	/** Entities only exist in HttpPut, HttpPatch, HttpPost */
	HotPadsHttpRequest setEntity(Map<String, String> entity) {
		if (entity != null && canHaveEntity()) {
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
			this.params.putAll(params);
		}
		return this;
	}

	public Map<String,String> getPostParams() {
		return params;
	}

	public boolean canHaveEntity() {
		return this.request instanceof HttpEntityEnclosingRequest;
	}

	public HotPadsHttpRequest addGetParams(Map<String,String> params) {
		if(params == null || params.isEmpty()) {
			return this;
		}
		StringBuilder queryString = new StringBuilder();
		for(Entry<String,String> param : params.entrySet()) {
			String key = param.getKey();
			if(key == null || key.trim().isEmpty()) {
				continue;
			}
			queryString.append('&' + urlEncode(key.trim()) + '=' + urlEncode(param.getValue()));
		}
		String paramStr = hasQueryString ? queryString.toString() : '?' + queryString.substring(1);
		try {
			request.setURI(new URI(request.getURI().toString() + paramStr));
			hasQueryString = true;
		} catch (URISyntaxException e) {}
		return this;
	}
	
	private List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
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
}
