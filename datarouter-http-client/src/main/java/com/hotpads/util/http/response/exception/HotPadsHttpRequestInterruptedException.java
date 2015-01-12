package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestInterruptedException extends HotPadsHttpException {
	
	public HotPadsHttpRequestInterruptedException(Exception e) {
		super("HTTP request interrupted", e);
	}
}
