package com.hotpads.handler.account;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.http.security.ApiKeyPredicate;

@Singleton
public class DatarouterAccountApiKeyPredicate implements ApiKeyPredicate{

	@Inject
	private DatarouterAccountService datarouterAccountService;

	@Override
	public boolean check(String apiKeyCandidate){
		return datarouterAccountService.findAccountForApiKey(apiKeyCandidate).isPresent();
	}

	@Override
	public String getApiKey(){
		throw new NotImplementedException();
	}

}
