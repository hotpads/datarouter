package com.hotpads.example.config.http;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.DatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.BaseDatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;

@Singleton
public class ExampleAuthenticationConfig extends BaseDatarouterAuthenticationConfig{

	@Override
	public Iterable<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response){
		return Arrays.asList();
	}

	@Override
	public Collection<DatarouterUserRole> getRequiredRoles(String path){
		return Arrays.asList();
	}

}
