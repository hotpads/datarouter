package com.hotpads.handler.user.authenticate.validation;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.DefaultSignatureValidator;
import com.hotpads.util.http.security.SignatureValidator;

@Singleton
public class UserSignatureValidator implements SignatureValidator{

	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	public boolean checkHexSignatureMulti(Map<String,String[]> params, String candidateSignature, String apiKey){
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		DefaultSignatureValidator signatureValidator = new DefaultSignatureValidator(user.getSecretKey());
		return signatureValidator.checkHexSignatureMulti(params, candidateSignature, apiKey);
	}

}
