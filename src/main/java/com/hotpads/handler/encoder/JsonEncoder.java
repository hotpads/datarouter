package com.hotpads.handler.encoder;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.util.http.client.json.GsonJsonSerializer;
import com.hotpads.util.http.client.json.JsonSerializer;

public class JsonEncoder implements Encoder{

	private JsonSerializer jsonSerializer;
	
	public JsonEncoder(){
		this.jsonSerializer = new GsonJsonSerializer();
	}
	
	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException{
		response.setContentType("application/json");
		response.getWriter().append(jsonSerializer.serialize(result));
	}

}
