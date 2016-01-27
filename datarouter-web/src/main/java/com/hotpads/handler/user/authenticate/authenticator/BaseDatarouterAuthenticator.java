package com.hotpads.handler.user.authenticate.authenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class BaseDatarouterAuthenticator
implements DatarouterAuthenticator{
	
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	protected BaseDatarouterAuthenticator(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	
}
