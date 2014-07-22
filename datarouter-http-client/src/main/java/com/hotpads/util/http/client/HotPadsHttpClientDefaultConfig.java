package com.hotpads.util.http.client;

import java.util.ArrayList;
import java.util.List;

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
	public List<Integer> getSuccessStatusCodes(){
		List<Integer> codes = new ArrayList<>();
		codes.add(200);
		codes.add(201);
		codes.add(204);
		return codes;
	}

}
