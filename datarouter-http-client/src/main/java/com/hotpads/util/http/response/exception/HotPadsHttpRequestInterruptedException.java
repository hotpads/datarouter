package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestInterruptedException extends HotPadsHttpException {
	
	public HotPadsHttpRequestInterruptedException(Exception e, long requestExecutedMs) {
		super("HTTP request interrupted after " + (System.currentTimeMillis() - requestExecutedMs) + "ms", e);
	}
}
