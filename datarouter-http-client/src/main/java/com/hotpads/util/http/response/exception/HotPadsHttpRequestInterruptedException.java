package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestInterruptedException extends HotPadsHttpException {
	
	public HotPadsHttpRequestInterruptedException(Exception ex, long requestStartTimeMs) {
		super("HTTP request interrupted after " + (System.currentTimeMillis() - requestStartTimeMs) + "ms", ex);
	}
}
