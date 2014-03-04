package com.hotpads.handler.user.authenticate.config;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.hotpads.handler.DatarouterCookieKeys;

public abstract class BaseDatarouterAuthenticationConfig
implements DatarouterAuthenticationConfig{
	
	public static final String
		PATH_SIGNUP_FORM = "/signup",
		PATH_SIGNUP_SUBMIT = "/signup/submit",
		PATH_SIGNIN_FORM = "/signin",
		PATH_SIGNIN_SUBMIT = "/signin/submit",
		PATH_SIGNOUT = "/signout",
		
		PARAM_USERNAME = "signinUsername",
		PARAM_PASSWORD = "signinPassword",
		PARAM_USERROLES = "userRoles",
		PARAM_ENABLED = "isEnabled",
		PARAM_USERID = "userId";
	
	@Override
	public String getSignupFormPath(){
		return PATH_SIGNUP_FORM;
	}
	
	@Override
	public String getSignupSubmitPath(){
		return PATH_SIGNUP_SUBMIT;
	}

	@Override
	public String getSigninFormPath(){
		return PATH_SIGNIN_FORM;
	}

	@Override
	public String getSigninSubmitPath(){
		return PATH_SIGNIN_SUBMIT;
	}
	
	@Override
	public String getSignoutPath(){
		return PATH_SIGNOUT;
	}

	@Override
	public String getUsernameParam(){
		return PARAM_USERNAME;
	}

	@Override
	public String getPasswordParam(){
		return PARAM_PASSWORD;
	}
	
	@Override
	public String getUserRolesParam() {
		return PARAM_USERROLES;
	}
	
	@Override
	public String getEnabledParam() {
		return PARAM_ENABLED;
	}
	
	public String getUserIdParam() {
		return PARAM_USERID;
	}
	
	@Override
	public String getUserTokenCookieName(){
		return DatarouterCookieKeys.userToken.toString();
	}
	
	@Override
	public String getSessionTokenCookieName(){
		return DatarouterCookieKeys.sessionToken.toString();
	}
	
	@Override
	public Integer getUserTokenTimeoutSeconds(){
		return 365 * 24 * 60 * 60;//365 days * 24 hours * 60 minutes * 60 seconds => 1 year
	}
	
	@Override
	public Integer getSessionTokenTimeoutSeconds(){
		return 30 * 60;//30 minutes * 60 seconds => 30 minutes
	}
	
	public static String normalizePath(String rawPath){
		Preconditions.checkNotNull(rawPath);
		String path = rawPath.trim().toLowerCase();
		//not scrubbing out duplicate slashes.  should we?
		Preconditions.checkArgument(path.startsWith("/"));
		if(path.length() > 1 && path.endsWith("/")){
			return path.substring(0, path.length() - 1);//remove trailing slash
		}
		return path;
	}
	
	public boolean isLoginRelatedPath(String path){
		return pathAContainsB(getSignupFormPath(), path)
				|| pathAContainsB(getSignupSubmitPath(), path)
				|| pathAContainsB(getSigninFormPath(), path)
				|| pathAContainsB(getSigninSubmitPath(), path)
				|| pathAContainsB(getSignoutPath(), path);
	}
	
	public static boolean pathAContainsB(String rawA, String rawB){
		String a = normalizePath(rawA);
		String b = normalizePath(rawB);
		if(a.equals(b)){ return true; }
		
		// a=/fl should NOT contain b=/flowbee
		String aAsDirectory = a + "/";
		return b.startsWith(aAsDirectory);
	}
	
//	@Override
//	public Iterable<BaseDatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
//			HttpServletResponse response){
//
//		List<DatarouterAuthenticator> authenticators = ListTool.createArrayList();
//		authenticators.add(new DatarouterLoginFormAuthenticator(request, response, HotPadsAuthenticationFilter.loginSubmitURI, PARAM_USERNAME,
//				PARAM_PASSWORD, routers.user(), routers.userSearch(), routers.event(), userItemRecordDao));
//
//		authenticators.add(new SessionAuthenticator(request, response));
//		authenticators.add(new RememberMeCookieAuthenticator(request, response));
//		authenticators.add(new UserTokenAuthenticator(request, response));
//		authenticators.add(new NewUserAuthenticator(request, response));
//		return authenticators;
//	}
	
	
	/***************** tests ************************/
	
	public static class BaseDatarouterAuthenticationConfigTests{
		@Test
		public void testNormalize(){
			Assert.assertEquals("/", normalizePath("/"));
			Assert.assertEquals("/", normalizePath(" / "));
			Assert.assertEquals("/caterpillar", normalizePath("/caterpillar"));
			Assert.assertEquals("/caterpillar", normalizePath("/caterpillar/"));
			Assert.assertEquals("/caterpillar/", normalizePath("/caterpillar//"));//prob not valid
		}
		@Test
		public void testContains(){
			Assert.assertTrue(pathAContainsB("/fl", "/fl"));
			Assert.assertTrue(pathAContainsB("/fl", "/fl/"));
			Assert.assertTrue(pathAContainsB("/fl/", "/fl/"));
			Assert.assertTrue(pathAContainsB("/fl", "/fl/owbee"));
			Assert.assertFalse(pathAContainsB("/fl", "/flowbee"));
			Assert.assertFalse(pathAContainsB("/flowbee", "/fl"));
		}
	}
	
}
