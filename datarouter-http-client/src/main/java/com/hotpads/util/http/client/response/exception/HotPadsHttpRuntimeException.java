package com.hotpads.util.http.client.response.exception;

import com.hotpads.util.http.client.response.HotPadsHttpResult;

@SuppressWarnings("serial")
public class HotPadsHttpRuntimeException extends RuntimeException implements HotPadsHttpResult {
	public HotPadsHttpRuntimeException(HotPadsHttpException e) {
		super(e);
	}
}
