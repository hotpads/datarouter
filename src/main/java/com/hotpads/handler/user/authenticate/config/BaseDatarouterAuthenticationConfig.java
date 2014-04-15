package com.hotpads.handler.user.authenticate.config;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.util.core.StringTool;

public abstract class BaseDatarouterAuthenticationConfig
implements DatarouterAuthenticationConfig{
	
	private static final String PATH_home = "/";
	private static final String PATH_keepalive = "/keepalive";
	private static final String PATH_datarouter = "/datarouter";
	private static final String PATH_api = "/api";
	
	private static final String PATH_signup = "/signup";
	private static final String PATH_signupSubmit = PATH_signup + "/submit";
	private static final String PATH_signin = "/signin";
	private static final String PATH_signinSubmit = PATH_signin + "/submit";
	private static final String PATH_signout = "/signout";
		
	private static final String PATH_resetPassword = "/resetPassword";
	private static final String PATH_resetPasswordSubmit = "/resetPasswordSubmit";

	private static final String PATH_admin = "/admin";
	private static final String PATH_adminViewUsers = PATH_admin + "/viewUsers";
	private static final String PATH_adminCreateUser = PATH_admin + "/createUser";
	private static final String PATH_adminCreateUserSubmit = PATH_admin + "/createUserSubmit";
	private static final String PATH_adminEditUser = PATH_admin + "/editUser";
	private static final String PATH_adminEditUserSubmit = PATH_admin + "/editUserSubmit";
	private static final String PATH_adminResetUserApiKey = PATH_admin + "/resetUserApiKey";

	private static final String PARAM_apiKey = "apiKey";
	private static final String PARAM_username = "signinUsername";
	private static final String PARAM_password = "signinPassword";
	private static final String PARAM_userRoles = "userRoles";
	private static final String PARAM_enabled = "isEnabled";
	private static final String PARAM_userId = "userId";
	private static final String PARAM_apiEnabled = "isApiEnabled";
	
	private static final String JSP_ROOT = "/WEB-INF/jsp";
	
	private static final String JSP_HOME = JSP_ROOT + "/home.jsp";
	private static final String JSP_KEEP_ALIVE = "/generic/keepAliveTest.jsp";

	private static final String JSP_ADMIN = "/admin";
	private static final String JSP_VIEW_USERS = JSP_ROOT + JSP_ADMIN + "/viewUsers.jsp";
	private static final String JSP_EDIT_USER = JSP_ROOT + JSP_ADMIN + "/editUsers.jsp";
	private static final String JSP_RESET_PASSWORD = JSP_ROOT + JSP_ADMIN + "/resetPasswordForm.jsp";
	
	@Override
	public String getHomePath() {
		return PATH_home;
	}
	
	@Override
	public String getKeepalivePath() {
		return PATH_keepalive;
	}
	
	@Override
	public String getDatarouterPath() {
		return PATH_datarouter;
	}
	
	@Override
	public String getApiPath() {
		return PATH_api;
	}
	
	/*********************** signin/out/up ************************************/

	@Override
	public String getSignupPath(){
		return PATH_signup;
	}
	
	@Override
	public String getSignupSubmitPath(){
		return PATH_signupSubmit;
	}

	@Override
	public String getSigninPath(){
		return PATH_signin;
	}

	@Override
	public String getSigninSubmitPath(){
		return PATH_signinSubmit;
	}
	
	@Override
	public String getSignoutPath(){
		return PATH_signout;
	}

	/*********************** password ************************************/
	
	@Override
	public String getResetPasswordPath() {
		return PATH_resetPassword;
	}

	@Override
	public String getResetPasswordSubmitPath() {
		return PATH_resetPasswordSubmit;
	}
	
	/*********************** admin ************************************/

	@Override
	public String getAdminPath() {
		return PATH_admin;
	}
	
	@Override
	public String getAdminViewUsersPath() {
		return PATH_adminViewUsers;
	}
	
	@Override
	public String getAdminCreateUserPath() {
		return PATH_adminCreateUser;
	}
	
	@Override
	public String getAdminCreateUserSubmitPath() {
		return PATH_adminCreateUserSubmit;
	}
	
	@Override
	public String getAdminEditUserPath() {
		return PATH_adminEditUser;
	}
	
	@Override
	public String getAdminEditUserSubmitPath() {
		return PATH_adminEditUserSubmit;
	}
	
	@Override
	public String getAdminResetUserApiKeyPath() {
		return PATH_adminResetUserApiKey;
	}

	/*********************** params ************************************/

	@Override
	public String getApiKeyParam() {
		return PARAM_apiKey;
	}
	
	@Override
	public String getUsernameParam(){
		return PARAM_username;
	}

	@Override
	public String getPasswordParam(){
		return PARAM_password;
	}
	
	@Override
	public String getUserRolesParam() {
		return PARAM_userRoles;
	}
	
	@Override
	public String getEnabledParam() {
		return PARAM_enabled;
	}
	
	@Override
	public String getUserIdParam() {
		return PARAM_userId;
	}
	
	@Override
	public String getApiEnabledParam() {
		return PARAM_apiEnabled;
	}
	
	/*********************** methods ************************************/

	@Override
	public Integer getUserTokenTimeoutSeconds(){
		return 365 * 24 * 60 * 60;//365 days * 24 hours * 60 minutes * 60 seconds => 1 year
	}
	
	@Override
	public Integer getSessionTokenTimeoutSeconds(){
		return 30 * 60;//30 minutes * 60 seconds => 30 minutes
	}
	
	@Override
	public String getCookiePrefix() {
		return "";
	}
	
	@Override
	public String getUserTokenCookieName(){
		return addCookiePrefix(DatarouterCookieKeys.userToken.toString());
	}
	
	@Override
	public String getSessionTokenCookieName(){
		return addCookiePrefix(DatarouterCookieKeys.sessionToken.toString());
	}
	
	@Override
	public String getTargetUrlName() {
		return addCookiePrefix(DatarouterCookieKeys.targetUrl.toString());
	}
	
	private String addCookiePrefix(String cookieName) {
		return getCookiePrefix() + StringTool.capitalizeFirstLetter(cookieName);
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
		return pathAContainsB(getSignupPath(), path)
				|| pathAContainsB(getSignupSubmitPath(), path)
				|| pathAContainsB(getSigninPath(), path)
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
