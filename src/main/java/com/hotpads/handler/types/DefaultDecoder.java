package com.hotpads.handler.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.client.json.GsonJsonSerializer;

public class DefaultDecoder implements HandlerDecoder{
	
	private GsonJsonSerializer deserializer;

	public DefaultDecoder(){
		this.deserializer = new GsonJsonSerializer();
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String, String[]> parameters = request.getParameterMap();
		LinkedHashMap<String, Type> expectedParameters = getMethodParameters(method);
		if(parameters.size() < expectedParameters.size()){
			return null;
		}
		
		Object[] args = new Object[expectedParameters.size()];
		int i = 0;
		for(Entry<String, Type> expectedParameter : expectedParameters.entrySet()){
			if(!parameters.containsKey(expectedParameter.getKey())){
				return null;
			}
			args[i] = deserializer.deserialize(parameters.get(expectedParameter.getKey())[0], expectedParameter.getValue());
			i++;
		}
		
		return args;
	}
	
	private LinkedHashMap<String, Type> getMethodParameters(Method method){
		LinkedHashMap<String, Type> parameters = new LinkedHashMap<String, Type>();
		int i = 0;
		Class<?>[] types = method.getParameterTypes();
		for(Annotation[] parameterAnnotations : method.getParameterAnnotations()){
			for(Annotation parameterAnnotation : parameterAnnotations){
				if(!parameterAnnotation.annotationType().equals(P.class)){
					continue;
				}
				P a = (P)parameterAnnotation;
				Type clazz = types[i];
				if(!a.typeProvider().equals(Object.class)){
					TypeProvider typeProvider = (TypeProvider) ReflectionTool.create(a.typeProvider());
					clazz = typeProvider.get();
				}
				parameters.put(a.value(), clazz);
			}
			i++;
		}
		return parameters;
	}

}