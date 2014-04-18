package com.hotpads.util.http.client.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GsonJsonSerializer implements JsonSerializer{
	
	private Gson gson;
	
	public GsonJsonSerializer(){
		gson = new Gson();
	}

	@Override
	public <T> String serialize(T toSerialize){
		return gson.toJson(toSerialize);
	}

	@Override
	public <T> T deserialize(String toDeserialize, Class<T> classOfT) throws JsonSyntaxException {
		return gson.fromJson(toDeserialize, classOfT);
	}
}
