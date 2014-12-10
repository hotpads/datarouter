package com.hotpads.util.http.client.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestExecutionException extends HotPadsHttpException {
	
	public HotPadsHttpRequestExecutionException(Exception e) {
		super("Exception occurred during HTTP request execution", e);
	}
}
