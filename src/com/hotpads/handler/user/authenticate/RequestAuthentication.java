package com.hotpads.handler.user.authenticate;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Preconditions;
import com.hotpads.util.core.CollectionTool;

@Deprecated//use DatarouterSession
public class RequestAuthentication{

	private static final String REQUEST_ATTRIBUTE_NAME = "requestAuthentication";
	
	
	/*************** fields **************/
	
	private String email;
	private Collection<DatarouterUserRole> roles;
	
	
	/************* construct **********************/
	
	public RequestAuthentication(String email, Collection<DatarouterUserRole> roles){
		this.email = Preconditions.checkNotNull(email);
		this.roles = Preconditions.checkNotNull(roles);
	}
	
	/**************** static methods ****************/
	
	public static void cacheInRequest(HttpServletRequest request, RequestAuthentication authentication) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, authentication); 
	}
	
	
	/******************* methods ********************/
	
	public boolean isAnonymous(){
		if(CollectionTool.isEmpty(roles)){ 
			throw new RuntimeException("no user roles found");
		}
		if(CollectionTool.size(roles) > 1 && roles.contains(DatarouterUserRole.anonymous)){
			throw new RuntimeException("found anonymous mixed with other roles");
		}
		return DatarouterUserRole.anonymous == CollectionTool.getFirst(roles);
	}
	
	
	/****************** get/set ********************/

	public Collection<DatarouterUserRole> getRoles(){
		return roles;
	}

	public String getEmail(){
		return email;
	}
	
}
