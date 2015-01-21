package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.guice.DatarouterGuiceModule.HandlerDefaultSerializer;
import com.hotpads.util.http.json.JsonSerializer;

@Singleton
public class JsonEncoder implements HandlerEncoder{

	private JsonSerializer jsonSerializer;
	
	@Inject
	public JsonEncoder(@HandlerDefaultSerializer JsonSerializer jsonSerializer){
		this.jsonSerializer = jsonSerializer;
	}
	
	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException{
		response.setContentType("application/json");
		response.getWriter().append(jsonSerializer.serialize(result));
	}

}
