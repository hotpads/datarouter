package com.hotpads.util.http.security;

public interface ApiKeyPredicate{

	boolean check(String parameter);

	String getApiKey();

}
