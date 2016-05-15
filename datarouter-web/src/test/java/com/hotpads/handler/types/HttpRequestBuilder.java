package com.hotpads.handler.types;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestBuilder{

	private final Map<String,String[]> parameterMap = new HashMap<>();
	private Reader reader;

	public HttpRequestBuilder withParameter(String key, String value){
		parameterMap.put(key, new String[]{value});
		return this;
	}

	public HttpRequestBuilder withBody(String string){
		reader = new StringReader(string);
		return this;
	}

	public MockHttpRequest build(){
		return new MockHttpRequest(parameterMap, reader);
	}

}
