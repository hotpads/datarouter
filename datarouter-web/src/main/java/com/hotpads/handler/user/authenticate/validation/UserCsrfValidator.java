package com.hotpads.handler.user.authenticate.validation;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultCsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;

public class UserCsrfValidator implements CsrfValidator{

	private Long requestTimeoutMs;
	private DatarouterUserNodes userNodes;

	public UserCsrfValidator(DatarouterUserNodes userNodes, Long requestTimeoutMs){
		this.requestTimeoutMs = requestTimeoutMs;
		this.userNodes = userNodes;
	}

	@Override
	public boolean check(HttpServletRequest request){
		return getCsrfValidatorForUserWithApiKey(request).check(request);
	}

	@Override
	public Long getRequestTimeMs(HttpServletRequest request){
		return getCsrfValidatorForUserWithApiKey(request).getRequestTimeMs(request);
	}

	private DefaultCsrfValidator getCsrfValidatorForUserWithApiKey(HttpServletRequest request){
		String apiKey = request.getParameter(SecurityParameters.API_KEY);
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
		return new DefaultCsrfValidator(user.getSecretKey(), requestTimeoutMs);
	}

}
