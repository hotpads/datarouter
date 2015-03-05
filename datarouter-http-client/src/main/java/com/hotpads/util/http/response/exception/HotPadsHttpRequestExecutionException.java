package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestExecutionException extends HotPadsHttpException {
	
	public HotPadsHttpRequestExecutionException(Exception ex, long requestStartTimeMs) {
		super("Exception occurred during HTTP request execution after "
				+ (System.currentTimeMillis() - requestStartTimeMs) + "ms", ex);
	}
}
