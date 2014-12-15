package com.hotpads.util.http.response;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.http.response.exception.HotPadsHttpException;

/**
 * This class is an abstraction over the HttpResponse that handles several of the expected HTTP failures
 */
public class HotPadsHttpResponse implements HotPadsHttpResult {
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpResponse.class);
	
	private int statusCode;
	private String entity;
	private HotPadsHttpException exception;
	
	public HotPadsHttpResponse(HttpResponse response) {
		if(response != null) {
			this.statusCode = response.getStatusLine().getStatusCode();
			this.entity = "";
			
			HttpEntity httpEntity = response.getEntity();
			if(httpEntity != null) {
				try{
					this.entity = EntityUtils.toString(httpEntity);
				} catch (final IOException ignore) {
				} finally {
					EntityUtils.consumeQuietly(httpEntity);
				}
			}
		}
	}
	
	public HotPadsHttpResponse(HotPadsHttpException exception) {
		this.exception = exception;
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public boolean isSuccessful() {
		return exception == null && statusCode < 300;
	}
	
	public <T extends Exception> void raiseException(boolean condition, T e) throws T {
		if(condition) {
			throw e;
		}
	}
}
