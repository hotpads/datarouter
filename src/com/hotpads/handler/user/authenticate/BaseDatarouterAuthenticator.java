package com.hotpads.handler.user.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


public abstract class BaseDatarouterAuthenticator
implements DatarouterAuthenticator{

	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	protected BaseDatarouterAuthenticator(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	
}
