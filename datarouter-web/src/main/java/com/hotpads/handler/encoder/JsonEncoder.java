package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.HandledException;
import com.hotpads.util.http.ResponseTool;
import com.hotpads.util.http.json.JsonSerializer;

@Singleton
public class JsonEncoder implements HandlerEncoder{

	private final JsonSerializer jsonSerializer;

	@Inject
	public JsonEncoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer jsonSerializer){
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws IOException{
		response.setContentType(ResponseTool.CONTENT_TYPE_APPLICATION_JSON);
		response.getWriter().append(jsonSerializer.serialize(result));
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request){
		ResponseTool.sendErrorInJson(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
	}

}
