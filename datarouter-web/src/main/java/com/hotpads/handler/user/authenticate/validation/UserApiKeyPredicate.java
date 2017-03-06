package com.hotpads.handler.user.authenticate.validation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.http.security.ApiKeyPredicate;

@Singleton
public class UserApiKeyPredicate implements ApiKeyPredicate{

	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	public boolean check(String apiKeyCandidate){
		if(DrStringTool.isNullOrEmpty(apiKeyCandidate)){
			return false;
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKeyCandidate),
				null);
		if(user == null || DrBooleanTool.isFalseOrNull(user.getEnabled())
				|| DrBooleanTool.isFalseOrNull(user.getApiEnabled())){
			return false;
		}
		return true;
	}

	@Override
	public String getApiKey(){
		throw new NotImplementedException();
	}

}
