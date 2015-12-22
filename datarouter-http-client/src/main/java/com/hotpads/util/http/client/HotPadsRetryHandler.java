package com.hotpads.util.http.client;

import java.io.IOException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotPadsRetryHandler implements HttpRequestRetryHandler{
	private static final Logger logger = LoggerFactory.getLogger(HotPadsRetryHandler.class);

	public static final String RETRY_SAFE_ATTRIBUTE = "retrySafe";
	private static final int DEFAULT_RETRY_COUNT = 2;

	private int retryCount;
	private boolean logOnRetry;

	public HotPadsRetryHandler(){
		retryCount = DEFAULT_RETRY_COUNT;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context){
		if(logOnRetry){
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			logger.warn("Request {} failure Nº {}", clientContext.getRequest().getRequestLine(), executionCount,
					exception);
		}
		Object retrySafe = context.getAttribute(RETRY_SAFE_ATTRIBUTE);
		if(retrySafe == null || !(retrySafe instanceof Boolean) || !(Boolean)retrySafe || executionCount > retryCount){
			return false;
		}
		return true;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}

	public void serLogOnRetry(boolean logOnRetry){
		this.logOnRetry = logOnRetry;
	}

}
