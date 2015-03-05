package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestExecutionException extends HotPadsHttpException {
	
	public HotPadsHttpRequestExecutionException(Exception e, long requestExecutedMs) {
		super("Exception occurred during HTTP request execution after "
				+ (System.currentTimeMillis() - requestExecutedMs) + "ms", e);
	}
}
