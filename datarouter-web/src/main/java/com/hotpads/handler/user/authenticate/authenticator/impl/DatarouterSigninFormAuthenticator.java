package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.exception.IncorrectPasswordException;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.HotPadsHttpResponse;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class DatarouterSigninFormAuthenticator extends BaseDatarouterAuthenticator{
	private DatarouterAuthenticationConfig authenticationConfig;
	private DatarouterUserNodes userNodes;
	private DatarouterPasswordService passwordService;

	private HotPadsHttpClient httpClient;
	private Boolean isLive;

	public DatarouterSigninFormAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes,
			DatarouterPasswordService passwordService, DatarouterProperties datarouterProperties){
		super(request, response);
		this.authenticationConfig = authenticationConfig;
		this.userNodes = userNodes;
		this.passwordService = passwordService;
		this.httpClient = new HotPadsHttpClientBuilder().build();//TODO?
		//TODO put this and/or other external login config into authenticationConfig
		isLive = !ServerType.DEV.equals(datarouterProperties.getServerType().getPersistentString());
		isLive = true;//TODO remove (this forces okta for now)
	}

	@Override
	public DatarouterSession getSession(){
		//the usual case where we're not submitting the login form.  just skip this filter
		if(DrObjectTool.notEquals(request.getServletPath(), authenticationConfig.getSigninSubmitPath())){
			return null;
		}

		String username = RequestTool.get(request, authenticationConfig.getUsernameParam(), null);
		String password = RequestTool.get(request, authenticationConfig.getPasswordParam(), null);

		DatarouterUser user = lookupAndValidateUser(username, password);

		user.setLastLoggedIn(new Date());

		DatarouterSession session = DatarouterSession.createFromUser(user);



		//TODO correct roles?
		Set<DatarouterUserRole> oktaRoles = new HashSet<>(Arrays.asList(DatarouterUserRole.admin, DatarouterUserRole
				.datarouterAdmin));
		//TODO how to apply this to already logged in?
		if(isLive && session.getRoles().stream().anyMatch(oktaRoles::contains)){
			//TODO check okta
			String reqEntity = "{\"username\": \"" + username + "\",\"password\": \"" + password + "\",\"options\": "
					+ "{\"multiOptionalFactorEnroll\": true,\"warnBeforePasswordExpired\": true}}";
			System.out.println(reqEntity);
			HotPadsHttpRequest oktaRequest = new HotPadsHttpRequest(HttpRequestMethod.POST,
					"https://dev-907596.oktapreview.com/api/v1/authn", false);
			oktaRequest.setEntity(reqEntity, ContentType.APPLICATION_JSON);
			try{
				HotPadsHttpResponse oktaResponse = httpClient.execute(oktaRequest);
				String responseEntity = oktaResponse.getEntity();
				System.out.println(responseEntity);
				if(responseEntity != null && responseEntity.contains("\"status\":\"SUCCESS\"")){
					return session;
				}else{
					throw new InvalidCredentialsException("Failed to authenticate with Okta");
				}
			}catch(HotPadsHttpRuntimeException e){
				throw new InvalidCredentialsException("Failed to authenticate with Okta");
			}
		}else{
			if(!passwordService.isPasswordCorrect(user, password)){//TODO
				throw new IncorrectPasswordException("invalid password (" + username + ")");
			}
		}

		userNodes.getUserNode().put(user, null);

		return session;
	}


	private DatarouterUser lookupAndValidateUser(String username, String password){
		if(DrStringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUsernameLookup(username), null);

		if(user == null){
			throw new InvalidCredentialsException("user not found (" + username + ")");
		}
		if(DrBooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}
		if(DrStringTool.isEmpty(password)){
			throw new InvalidCredentialsException("password cannot be empty (" + username + ")");
		}
//		if(!passwordService.isPasswordCorrect(user, password)){//TODO remove?
//			throw new IncorrectPasswordException("invalid password (" + username + ")");
//		}
		return user;
	}
}
