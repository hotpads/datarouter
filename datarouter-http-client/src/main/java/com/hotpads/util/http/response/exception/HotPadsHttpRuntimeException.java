package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRuntimeException extends RuntimeException {
	public HotPadsHttpRuntimeException(Exception exception) {
		super(exception);
	}
	public HotPadsHttpRuntimeException(HotPadsHttpException exception) {
		super(exception.getMessage(), exception.getCause());
	}
}
