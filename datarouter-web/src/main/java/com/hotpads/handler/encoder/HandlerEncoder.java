package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.HandledException;

public interface HandlerEncoder{

	void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException;
	
	void sendExceptionResponse(HandledException exception, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException;

}
