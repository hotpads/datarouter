package com.hotpads.util.http.client.json;

public interface JsonSerializer{

	public <T> String serialize(T toSerialize);
	
	public <T> T deserialize(String toDeserialize, Class<T> classOfT);
	
}
