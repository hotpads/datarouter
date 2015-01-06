package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpRuntimeException extends RuntimeException {
	public HotPadsHttpRuntimeException(Exception e) {
		super(e);
	}
	public HotPadsHttpRuntimeException(HotPadsHttpException e) {
		super(e.getMessage(), e.getCause());
	}
}
