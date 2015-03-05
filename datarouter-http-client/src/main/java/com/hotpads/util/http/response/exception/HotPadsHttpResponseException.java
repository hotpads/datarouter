package com.hotpads.util.http.response.exception;

import org.apache.http.HttpStatus;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public class HotPadsHttpResponseException extends HotPadsHttpException {

	private HotPadsHttpResponse response;
	
	public HotPadsHttpResponseException(HotPadsHttpResponse response) {
		super("HTTP response returned with status code " + response.getStatusCode() + "\n" + response.getEntity(), null);
		this.response = response;
	}
	
	public HotPadsHttpResponse getResponse() {
		return response;
	}
	
	/**
	 * 4XX status code. Issue exists in the client or request.
	 */
	public boolean isClientError() {
		int statusCode = response.getStatusCode();
		return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}
	
	/**
	 * 5XX status code. Issue exists on the server.
	 */
	public boolean isServerError() {
		int statusCode = response.getStatusCode();
		return statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}
}