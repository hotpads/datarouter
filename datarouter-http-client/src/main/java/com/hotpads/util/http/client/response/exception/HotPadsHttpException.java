package com.hotpads.util.http.client.response.exception;

import com.hotpads.util.http.client.response.HotPadsHttpResult;

@SuppressWarnings("serial")
public abstract class HotPadsHttpException extends Exception implements HotPadsHttpResult {
	protected HotPadsHttpException(String message) {
		super(message);
	}
	
	protected HotPadsHttpException(String message, Exception e) {
		super(message, e);
	}
}
