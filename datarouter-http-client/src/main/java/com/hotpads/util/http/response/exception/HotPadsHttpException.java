package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public abstract class HotPadsHttpException extends Exception {
	protected HotPadsHttpException(String message, Exception e) {
		super(message, e);
	}
}
