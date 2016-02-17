package com.hotpads.util.http.response.exception;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public class HotPadsHttpResponseException extends HotPadsHttpException {

	public static final String X_EXCEPTION_ID = "x-eid";

	private final HotPadsHttpResponse response;

	public HotPadsHttpResponseException(HotPadsHttpResponse response, long requestStartTimeMs) {
		super(buildMessage(response, requestStartTimeMs), null);
		this.response = response;
	}

	private static String buildMessage(HotPadsHttpResponse response, long requestStartTimeMs){
		String message = "HTTP response returned with status code " + response.getStatusCode();
		Header header = response.getFirstHeader(X_EXCEPTION_ID);
		if(header != null){
			message += " and exception id " + header.getValue();
		}
		message += " after " + (System.currentTimeMillis() - requestStartTimeMs) + "ms";
		message += " with entity:\n" + response.getEntity();
		return message;
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