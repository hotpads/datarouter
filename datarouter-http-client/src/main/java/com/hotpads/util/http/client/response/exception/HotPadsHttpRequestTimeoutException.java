package com.hotpads.util.http.client.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestTimeoutException extends HotPadsHttpException {
	
	public HotPadsHttpRequestTimeoutException(Exception e, Integer timeoutMs) {
		super("HTTP request hit " + (timeoutMs == null ? "" : timeoutMs.toString() + "ms ") + "timeout", e);
	}
}
