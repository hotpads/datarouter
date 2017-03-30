package com.hotpads.handler.user.authenticate.validation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUserNodes;

@Singleton
public class UserCsrfValidatorFactory{

	@Inject
	private DatarouterUserNodes userNodes;

	public UserCsrfValidator create(Long requestTimeoutMs){
		return new UserCsrfValidator(userNodes, requestTimeoutMs);
	}
}
