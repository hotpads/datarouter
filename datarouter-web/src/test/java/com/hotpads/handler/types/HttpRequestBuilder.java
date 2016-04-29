package com.hotpads.handler.types;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestBuilder{

	private final Map<String,String[]> parameterMap = new HashMap<>();

	public HttpRequestBuilder withParameter(String key, String value){
		parameterMap.put(key, new String[]{value});
		return this;
	}

	public MockHttpRequest build(){
		return new MockHttpRequest(parameterMap);
	}

}
