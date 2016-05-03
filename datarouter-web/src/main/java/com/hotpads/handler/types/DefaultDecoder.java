package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonSyntaxException;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.json.JsonSerializer;

@Singleton
public class DefaultDecoder implements HandlerDecoder{

	//TODO Rename JsonSerializer or add Serializer, we just want a (de)serializer here
	private JsonSerializer deserializer;

	@Inject
	public DefaultDecoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer deserializer){
		this.deserializer = deserializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String, String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		String body = null;
		int bodyParamCount = 0;
		if(containRequestBodyParam(parameters)){
			body = RequestTool.getBodyAsString(request);
			if(DrStringTool.isEmpty(body)){
				return null;
			}
			bodyParamCount = 1;
		}
		if(queryParams.size() + bodyParamCount < parameters.length){
			return null;
		}
		Object[] args = new Object[parameters.length];
		for(int i = 0; i < parameters.length; i++){
			Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			Type parameterType = parameter.getType();
			{
				P parameterAnnotation = parameter.getAnnotation(P.class);
				if(parameterAnnotation != null){
					if(!parameterAnnotation.value().isEmpty()){
						parameterName = parameterAnnotation.value();
					}
					if(!parameterAnnotation.typeProvider().equals(TypeProvider.class)){
						parameterType = ReflectionTool.create(parameterAnnotation.typeProvider()).get();
					}
				}
			}
			boolean isBodyParameter = parameter.isAnnotationPresent(RequestBody.class);
			if(isBodyParameter){
				args[i] = decode(body, parameterType);
			}else{
				String[] queryParam = queryParams.get(parameterName);
				if(queryParam == null){
					return null;
				}
				args[i] = decode(queryParam[0], parameterType);
			}
		}
		return args;
	}

	private Object decode(String string, Type type){
		try{
			return deserializer.deserialize(string, type);
		}catch(JsonSyntaxException e){
			//If the JSON is malformed and String is expected, just assign the string
			if(type.equals(String.class)){
				return string;
			}
			throw e;
		}
	}

	private boolean containRequestBodyParam(Parameter[] parameters){
		return Arrays.stream(parameters)
				.anyMatch(parameter -> parameter.isAnnotationPresent(RequestBody.class));
	}

}
