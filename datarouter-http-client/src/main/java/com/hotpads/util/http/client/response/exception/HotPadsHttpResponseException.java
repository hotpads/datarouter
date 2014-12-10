package com.hotpads.util.http.client.response.exception;

import com.hotpads.util.http.client.response.HotPadsHttpResponse;
import com.hotpads.util.http.client.response.HotPadsHttpResult;

@SuppressWarnings("serial")
public class HotPadsHttpResponseException extends HotPadsHttpException implements HotPadsHttpResult {
	private HotPadsHttpResponse response;
	
	public HotPadsHttpResponseException(HotPadsHttpResponse response) {
		super("HTTP request returned with status code: " + response.getStatusCode());
		this.response = response;
	}
	
	public HotPadsHttpResponse getResponse() {
		return response;
	}
}
