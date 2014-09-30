package com.hotpads.util.http.client;

import java.io.IOException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

public class HotPadsRetryHandler implements HttpRequestRetryHandler{
	
	public static final String RETRY_SAFE_ATTRIBUTE = "retrySafe";
	private static final int DEFAULT_RETRY_COUNT = 2;
	
	private int retryCount;
	
	public HotPadsRetryHandler(){
		retryCount = DEFAULT_RETRY_COUNT;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context){
		Object retrySafe = context.getAttribute(RETRY_SAFE_ATTRIBUTE);
		if(retrySafe == null || !(retrySafe instanceof Boolean) || !(Boolean)retrySafe || executionCount > retryCount){
			return false;
		}
		return true;
	}
	
	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}
	
}
