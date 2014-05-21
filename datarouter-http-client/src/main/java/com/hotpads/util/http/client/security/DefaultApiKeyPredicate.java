package com.hotpads.util.http.client.security;

public class DefaultApiKeyPredicate implements ApiKeyPredicate{

	private String apiKey;

	public DefaultApiKeyPredicate(String apiKey){
		this.apiKey = apiKey;
	}

	public boolean check(String apiKeyCandidate){
		return apiKey.equals(apiKeyCandidate);
	}

	public String getApiKey(){
		return apiKey;
	}

}
