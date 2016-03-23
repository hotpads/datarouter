package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.json.JsonSerializer;

public class BodyDecoder implements HandlerDecoder{

	private final JsonSerializer deserializer;

	@Inject
	public BodyDecoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer deserializer){
		this.deserializer = deserializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Parameter[] parameters = method.getParameters();
		if(parameters.length != 1){
			return null;
		}
		String body = RequestTool.getBodyAsString(request);
		Class<?> type = parameters[0].getType();
		if(type == String.class){
			return new Object[]{body};
		}
		return new Object[]{deserializer.deserialize(body, type)};
	}

}
