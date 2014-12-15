package com.hotpads.util.http.response.exception;

import com.hotpads.util.http.response.HotPadsHttpResult;

@SuppressWarnings("serial")
public class HotPadsHttpRuntimeException extends RuntimeException implements HotPadsHttpResult {
	public HotPadsHttpRuntimeException(HotPadsHttpException e) {
		super(e);
	}
}
