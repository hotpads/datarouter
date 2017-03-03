package com.hotpads.handler.user.authenticate.validation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.DefaultCsrfValidator;

@Singleton
public class UserCsrfValidator extends DefaultCsrfValidator{

	private final DatarouterUserNodes userNodes;

	@Inject
	public UserCsrfValidator(DatarouterUserNodes userNodes){
		super(null);
		this.userNodes = userNodes;
	}

	@Override
	public boolean check(String csrfToken, String cipherIv, String apiKey){
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		this.cipherKey = user.getSecretKey();
		return super.check(csrfToken, cipherIv, apiKey);
	}

	@Override
	public Long getRequestTimeMs(String csrfToken, String cipherIv){
		return super.getRequestTimeMs(csrfToken, cipherIv);
	}

}
