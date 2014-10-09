package com.hotpads.util.http.client;

public class HotPadsHttpClientDefaultConfig implements HotPadsHttpClientConfig{

	@Override
	public String getDtoParameterName(){
		return "dataTransferObject";
	}

	@Override
	public String getDtoTypeParameterName(){
		return "dataTransferObjectType";
	}

	@Override
	public String getRequestParameterName() {
		return "requestParams";
	}
	
	@Override
	public String getResponseParameterName() {
		return "responseParams";
	}
}
