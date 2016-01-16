package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRequestFutureTimeoutException extends HotPadsHttpException {

	public HotPadsHttpRequestFutureTimeoutException(Exception exception, Integer timeoutMs) {
		super("HTTP request future hit " + (timeoutMs == null ? "" : timeoutMs + "ms ") + "timeout", exception);
	}

}
