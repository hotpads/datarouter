package com.hotpads.util.http.client;

public class HotpadsHttpClientDefaultConfig implements HotPadsHttpClientConfig{

	@Override
	public String getDtoParameterName(){
		return "dataTransferObject";
	}

	@Override
	public String getDtoTypeParameterName(){
		return "dataTransferObjectType";
	}

}
