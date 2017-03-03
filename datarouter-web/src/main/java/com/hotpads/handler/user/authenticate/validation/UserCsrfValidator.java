package com.hotpads.handler.user.authenticate.validation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultCsrfValidator;

@Singleton
public class UserCsrfValidator implements CsrfValidator{

	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	public boolean check(String csrfToken, String cipherIv, String apiKey){
		return getCsrfValidatorForUserWithApiKey(apiKey).check(csrfToken, cipherIv, apiKey);
	}

	@Override
	public Long getRequestTimeMs(String csrfToken, String cipherIv, String apiKey){
		return getCsrfValidatorForUserWithApiKey(apiKey).getRequestTimeMs(csrfToken, cipherIv, apiKey);
	}

	private DefaultCsrfValidator getCsrfValidatorForUserWithApiKey(String apiKey){
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		return new DefaultCsrfValidator(user.getSecretKey());
	}

}
