package com.hotpads.util.http.security;

import java.util.function.Supplier;

public class DefaultApiKeyPredicate implements ApiKeyPredicate{

	private Supplier<String> apiKeySupplier;

	public DefaultApiKeyPredicate(Supplier<String> apiKeySupplier){
		this.apiKeySupplier = apiKeySupplier;
	}

	public DefaultApiKeyPredicate(String apiKey){
		this.apiKeySupplier = () -> apiKey;
	}

	@Override
	public boolean check(String apiKeyCandidate){
		return apiKeySupplier.get().equals(apiKeyCandidate);
	}

	@Override
	public String getApiKey(){
		return apiKeySupplier.get();
	}

}
