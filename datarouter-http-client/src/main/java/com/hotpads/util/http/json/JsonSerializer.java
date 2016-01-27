package com.hotpads.util.http.json;

import java.lang.reflect.Type;

public interface JsonSerializer{

	public <T> String serialize(T toSerialize);
	
	public <T> T deserialize(String toDeserialize, Type classOfT);
	
}
