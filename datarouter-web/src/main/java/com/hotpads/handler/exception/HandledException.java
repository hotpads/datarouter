package com.hotpads.handler.exception;

/* Exceptions implementing this interface will be caught by the BaseHandler and returned to the client via an encoder.
 */
public interface HandledException{
	public String getMessage();
}