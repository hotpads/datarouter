package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestFutureTimeoutException extends HotPadsHttpException {

	public HotPadsHttpRequestFutureTimeoutException(Exception e, Integer timeoutMs) {
		super("HTTP request future hit " + (timeoutMs == null ? "" : timeoutMs.toString() + "ms ") + "timeout", e);
	}
}
