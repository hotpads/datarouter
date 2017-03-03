package com.hotpads.handler.user.authenticate.validation;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.DefaultSignatureValidator;

@Singleton
public class UserSignatureValidator extends DefaultSignatureValidator{

	private final DatarouterUserNodes userNodes;

	@Inject
	public UserSignatureValidator(DatarouterUserNodes userNodes){
		super(null);
		this.userNodes = userNodes;
	}

	@Override
	public boolean checkHexSignatureMulti(Map<String,String[]> params, String candidateSignature, String apiKey){
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		this.salt = user.getSecretKey();
		return super.checkHexSignatureMulti(params, candidateSignature, apiKey);
	}

}
