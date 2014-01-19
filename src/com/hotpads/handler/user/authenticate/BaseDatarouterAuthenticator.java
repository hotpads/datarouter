package com.hotpads.handler.user.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.handler.user.session.DatarouterSession;


public abstract class BaseDatarouterAuthenticator{
	protected Logger logger = Logger.getLogger(BaseDatarouterAuthenticator.class);

	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	protected BaseDatarouterAuthenticator(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	public abstract DatarouterSession getSession();
	
}
