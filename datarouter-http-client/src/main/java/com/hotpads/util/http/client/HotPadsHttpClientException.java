package com.hotpads.util.http.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("serial")
public class HotPadsHttpClientException extends RuntimeException {

	private HttpResponse response;

	public HotPadsHttpClientException(Exception e, HttpRequest request, HttpResponse response) {
		super(e);
		this.response = response;
	}

	public int getStatusCode() {
		if (response == null)
			return -1;
		return response.getStatusLine().getStatusCode();
	}

	public HttpResponse getResponse() {
		return response;
	}
	
	public String getEntity() {
		if(response == null || response.getEntity() == null) {
			return "";
		}
		
		HttpEntity httpEntity = response.getEntity();
		try{
			return EntityUtils.toString(httpEntity);
		} catch (final IOException ignore) {
			return "";
		} finally {
			EntityUtils.consumeQuietly(httpEntity);
		}
	}
}
