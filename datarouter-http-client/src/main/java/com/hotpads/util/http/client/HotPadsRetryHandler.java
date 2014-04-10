package com.hotpads.util.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

public class HotPadsRetryHandler implements HttpRequestRetryHandler{
	
	private static final int DEFAULT_RETRY_COUNT = 2;
	
	private int retryCount;
	private List<HttpRequest> requestToRetry;
	
	public HotPadsRetryHandler(){
		retryCount = DEFAULT_RETRY_COUNT;
		requestToRetry = Collections.synchronizedList(new ArrayList<HttpRequest>());
	}

	public void setRetrySafe(HttpRequest request){
		requestToRetry.add(request);
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext request){
		return requestToRetry.contains(request) && executionCount < retryCount;
	}
	
	public void clean(HttpRequest httpRequest){
		requestToRetry.remove(httpRequest);
	}

	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}
	
}
