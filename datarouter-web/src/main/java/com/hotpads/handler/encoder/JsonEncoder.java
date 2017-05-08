package com.hotpads.handler.encoder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.exception.HandledException;
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
		new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)
				.append(jsonSerializer.serialize(result))
				.close();
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request){
		Object responseBody = exception.getHttpResponseBody();
		if(responseBody == null){
			ResponseTool.sendErrorInJsonForMessage(response, exception.getHttpResponseCode(), exception.getMessage());
			return;
		}
		ResponseTool.sendErrorInJson(response, exception.getHttpResponseCode(), jsonSerializer.serialize(responseBody));
	}

}
