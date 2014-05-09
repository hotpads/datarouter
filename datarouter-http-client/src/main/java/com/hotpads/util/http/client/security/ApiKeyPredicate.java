package com.hotpads.util.http.client.security;

public interface ApiKeyPredicate{

	boolean check(String parameter);

	String getApiKey();

}
