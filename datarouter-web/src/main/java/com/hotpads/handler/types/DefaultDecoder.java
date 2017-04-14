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

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;
import com.hotpads.datarouter.test.DatarouterWebTestModuleFactory;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.handler.types.optional.OptionalParameter;
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
		long bodyParamCount = countRequestBodyParam(parameters);
		if(queryParams.size() + bodyParamCount + getOptionalParameterCount(parameters) < parameters.length){
			return null;
		}
		String body = null;
		if(bodyParamCount >= 1){
			body = RequestTool.getBodyAsString(request);
			if(DrStringTool.isEmpty(body)){
				return null;
			}
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
			if(parameter.isAnnotationPresent(RequestBody.class)){
				args[i] = decode(body, parameterType);
			}else if(parameter.isAnnotationPresent(RequestBodyString.class)){
				args[i] = body;
			}else{
				String[] queryParam = queryParams.get(parameterName);
				boolean isOptional = OptionalParameter.class.isAssignableFrom(parameter.getType());
				if(queryParam == null && !isOptional){
					return null;
				}
				String parameterValue = queryParam == null ? null : queryParam[0];
				args[i] = isOptional ? OptionalParameter.makeOptionalParameter(parameterValue, parameterType)
						: decode(parameterValue, parameterType);
			}
		}
		return args;
	}

	private Object decode(String string, Type type){
		//this prevents empty strings from being decoded as null by gson
		Object obj;
		try{
			obj = deserializer.deserialize(string, type);
		}catch(JsonSyntaxException e){
			//If the JSON is malformed and String is expected, just assign the string
			if(type.equals(String.class)){
				return string;
			}
			throw new RuntimeException("failed to decode " + string + " to a " + type, e);
		}
		//deserialized successfully as null, but we want empty string instead of null for consistency with Params
		//(unless it actually is null...)
		if(string != null && obj == null && type.equals(String.class) && !"null".equals(string)){
			return "";
		}
		return obj;
	}

	private static long countRequestBodyParam(Parameter[] parameters){
		return Arrays.stream(parameters)
				.filter(parameter -> parameter.isAnnotationPresent(RequestBody.class)
						|| parameter.isAnnotationPresent(RequestBodyString.class))
				.count();
	}

	private static long getOptionalParameterCount(Parameter[] parameters){
		return Arrays.stream(parameters)
				.filter(parameter -> OptionalParameter.class.isAssignableFrom(parameter.getType()))
				.count();
	}

	@Guice(moduleFactory = DatarouterWebTestModuleFactory.class)
	public static class DefaultDecoderTests{
		@Inject
		DefaultDecoder defaultDecoder;

		@Test
		public void testDecodingString(){
			Assert.assertEquals(defaultDecoder.decode("", String.class), "");
			Assert.assertEquals(defaultDecoder.decode(" ", String.class), "");
			Assert.assertEquals(defaultDecoder.decode("\"\"", String.class), "");
			Assert.assertEquals(defaultDecoder.decode("\"", String.class), "\"");
			Assert.assertEquals(defaultDecoder.decode("\" ", String.class), "\" ");
			Assert.assertEquals(defaultDecoder.decode("\" \"", String.class), " ");
			Assert.assertEquals(defaultDecoder.decode("null", String.class), null);
			Assert.assertEquals(defaultDecoder.decode(null, String.class), null);
			Assert.assertEquals(defaultDecoder.decode("nulls", String.class), "nulls");
			Assert.assertEquals(defaultDecoder.decode("\"correct json\"", String.class), "correct json");
			Assert.assertEquals(defaultDecoder.decode("", Integer.class), null);
			Assert.assertEquals(defaultDecoder.decode(" ", Integer.class), null);
		}
	}
}
