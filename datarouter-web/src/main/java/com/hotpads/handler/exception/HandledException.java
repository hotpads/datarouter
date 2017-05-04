package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Exceptions implementing this interface will be caught by the BaseHandler and returned to the client via an encoder.
 */
public interface HandledException{

	String getMessage();

	default int getHttpResponseCode(){
		return HttpServletResponse.SC_BAD_REQUEST;
	}

	default Object getHttpResponseBody(){
		return null;
	}

}
