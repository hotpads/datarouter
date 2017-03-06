package com.hotpads.handler.user.authenticate.validation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.DefaultSignatureValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

@Singleton
public class UserSignatureValidator implements SignatureValidator{

	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	public boolean checkHexSignatureMulti(HttpServletRequest request){
		String apiKey = request.getParameter(SecurityParameters.API_KEY);
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		DefaultSignatureValidator signatureValidator = new DefaultSignatureValidator(user.getSecretKey());
		return signatureValidator.checkHexSignatureMulti(request);
	}

}
