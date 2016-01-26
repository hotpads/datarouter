package com.hotpads.handler.types;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.util.core.java.ReflectionTool;
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
		Map<String, String[]> parameters = request.getParameterMap();
		LinkedHashMap<String, Type> expectedParameters = getMethodParameters(method);
		if(parameters.size() < expectedParameters.size()){
			return null;
		}

		Object[] args = new Object[expectedParameters.size()];
		int index = 0;
		for(Entry<String, Type> expectedParameter : expectedParameters.entrySet()){
			if(!parameters.containsKey(expectedParameter.getKey())){
				return null;
			}
			try{
				args[index] = deserializer.deserialize(parameters.get(expectedParameter.getKey())[0],
						expectedParameter.getValue());
			}catch(JsonSyntaxException e){
				//If the JSON is malformed and String is expected, just assign the string
				if(!expectedParameter.getValue().equals(String.class)){
					throw e;
				}
				args[index] = parameters.get(expectedParameter.getKey())[0];
			}
			index++;
		}

		return args;
	}

	private LinkedHashMap<String, Type> getMethodParameters(Method method){
		LinkedHashMap<String, Type> parameters = new LinkedHashMap<>();
		for(Parameter parameter : method.getParameters()){
			String parameterName = parameter.getName();
			Type parameterType = parameter.getType();
			P parameterAnnotation = parameter.getAnnotation(P.class);
			if(parameterAnnotation != null){
				if(!parameterAnnotation.value().isEmpty()){
					parameterName = parameterAnnotation.value();
				}
				if(!parameterAnnotation.typeProvider().equals(Object.class)){
					parameterType = ((TypeProvider)ReflectionTool.create(parameterAnnotation.typeProvider())).get();
				}
			}
			parameters.put(parameterName, parameterType);
		}
		return parameters;
	}

	public static class DefaultDecoderTests{

		/**
		 * Used via reflection in testMethodParameterNameInclusionAtRuntime
		 */
		@SuppressWarnings("unused")
		private void myMethod(String myParameter){

		}

		@Test
		public void testMethodParameterNameInclusionAtRuntime() throws NoSuchMethodException, SecurityException{
			Method method = DefaultDecoderTests.class.getDeclaredMethod("myMethod", String.class);
			Assert.assertNotNull(method);
			Assert.assertEquals("myParameter", method.getParameters()[0].getName());
		}
	}

}
