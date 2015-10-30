package com.hotpads.util.http.json;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Singleton
public class GsonJsonSerializer implements JsonSerializer{
	private static final Logger logger = LoggerFactory.getLogger(GsonJsonSerializer.class);
	
	private Gson gson;
	
	public GsonJsonSerializer(){
		gson = new Gson();
	}
	
	public GsonJsonSerializer(Gson gson){
		this.gson = gson;
	}

	@Override
	public <T> String serialize(T toSerialize){
		return gson.toJson(toSerialize);
	}

	@Override
	public <T> T deserialize(String toDeserialize, Type returnType) throws JsonSyntaxException{
		try{
			T deserializedValue = gson.fromJson(toDeserialize, returnType);
			return deserializedValue;
		}catch(Exception e){
			logger.error("toDeserialize=" + toDeserialize, e);
			throw e;
		}
	}
}
