package com.hotpads.util.http.client;

import java.util.List;

public interface HotPadsHttpClientConfig{
	public String getDtoParameterName();
	public String getDtoTypeParameterName();
	public List<Integer> getSuccessStatusCodes();
}
