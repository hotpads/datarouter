package com.hotpads.util.http.response.exception;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public abstract class HotPadsHttpResponseException extends HotPadsHttpException {

	private HotPadsHttpResponse response;
	
	public HotPadsHttpResponseException(HotPadsHttpResponse response) {
		super("HTTP response returned with status code " + response.getStatusCode(), null);
		this.response = response;
	}
	
	public HotPadsHttpResponse getResponse() {
		return response;
	}
}