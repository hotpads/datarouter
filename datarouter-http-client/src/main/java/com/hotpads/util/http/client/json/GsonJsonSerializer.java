package com.hotpads.util.http.client.json;

import java.lang.reflect.Type;

import com.google.gson.Gson;

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
	public <T> T deserialize(String toDeserialize, Type classOfT){
		return gson.fromJson(toDeserialize, classOfT);
	}
}
