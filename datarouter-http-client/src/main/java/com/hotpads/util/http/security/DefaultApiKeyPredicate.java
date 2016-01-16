package com.hotpads.util.http.security;

public class DefaultApiKeyPredicate implements ApiKeyPredicate{

	private String apiKey;

	public DefaultApiKeyPredicate(String apiKey){
		this.apiKey = apiKey;
	}

	@Override
	public boolean check(String apiKeyCandidate){
		return apiKey.equals(apiKeyCandidate);
	}

	@Override
	public String getApiKey(){
		return apiKey;
	}

}
