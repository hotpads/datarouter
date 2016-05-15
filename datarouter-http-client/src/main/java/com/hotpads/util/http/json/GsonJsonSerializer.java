package com.hotpads.util.http.json;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import com.google.gson.Gson;

@Singleton
public class GsonJsonSerializer implements JsonSerializer{

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
	public <T> T deserialize(String toDeserialize, Type returnType){
		return gson.fromJson(toDeserialize, returnType);
	}
}
