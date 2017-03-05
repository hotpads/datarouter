package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.exception.HandledException;

public interface HandlerEncoder{

	public static final String DEFAULT_HANDLER_SERIALIZER = "defaultHandlerSerializer";

	void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException;

	void sendExceptionResponse(HandledException exception, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException;

}
