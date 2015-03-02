package com.hotpads.util.http.response.exception;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public class HotPadsHttpResponseException extends HotPadsHttpException {

	public HotPadsHttpResponseException(HotPadsHttpResponse response) {
		super("Request returned with status code " + response.getStatusCode(), null);
	}
}