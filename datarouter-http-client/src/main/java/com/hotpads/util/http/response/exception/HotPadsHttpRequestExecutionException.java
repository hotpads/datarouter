package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestExecutionException extends HotPadsHttpException {
	
	public HotPadsHttpRequestExecutionException(Exception e) {
		super("Exception occurred during HTTP request execution", e);
	}
}
