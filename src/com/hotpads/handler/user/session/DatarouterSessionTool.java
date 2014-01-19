package com.hotpads.handler.user.session;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.hotpads.handler.CookieTool;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class DatarouterSessionTool{
	
	/************* static fields **********************/
	
	private static SecureRandom secureRandom;
	static{
		try{
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("misconfigured - no secure random");
		}
	}
	
	
	/************* static methods *********************/
	
	public static String generateSessionToken(){
		byte[] sha1Bytes = new byte[32];
		secureRandom.nextBytes(sha1Bytes);
		byte[] sha256Bytes = DigestUtils.sha256(sha1Bytes);//further encode older sha1
		byte[] base64Bytes = Base64.encodeBase64URLSafe(sha256Bytes);
		String randomString = StringByteTool.fromUtf8Bytes(base64Bytes);
		return randomString;
	}
	
	public static String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, DatarouterCookieKeys.sessionToken.toString());
	}

	
	private final static String REQUEST_ATTRIBUTE_NAME = "userSession";
	
	public static DatarouterSession getUserSession(HttpServletRequest request) {
		//try to get userSession out of request cache
		DatarouterSession userSession = (DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		if (userSession != null) {
			return userSession;
		} else {
			String sessionToken = UserSessionTokenTool.getSessionTokenFromCookie(request);
			userSession = SessionDao.getUserSession(sessionToken);
			if (userSession != null)
				cacheInRequest(request, userSession);
			return userSession;
		}
	}
		
	public static void store(HttpServletRequest request, DatarouterSession userSession) {
		cacheInRequest(request, userSession);
		Set<UserRole> roles = SetTool.create();
		boolean rep = request.getAttribute(RequestKeys.REP.toString()) != null && ((Boolean)request.getAttribute(RequestKeys.REP.toString()));
		if (rep){ //dont store rep permissions for changedUser
			roles = SetTool.createHashSet(userSession.getUserRoles());
			roles.remove(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
		//store userSession in memcached for 3 days
		if (userSession != null) {
			if(userSession.getUserToken() == null){
				userSession.setUserToken(UserTool.getUserToken(request));
				userSession.setSessionToken(UserTool.getSessionToken(request));									
			}
			SessionDao.saveUserSession(userSession);
		}
		
		if (rep) {
			roles.add(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
	}
	
	public static void cacheInRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession); 
	}
	
	public static void populateAndCache(HttpServletRequest request, DatarouterSession userSession, User user){
		List<Authority> authorities = AuthorityDao.getAuthorities(user.getKey());
		userSession.setEmail(user.getEmail());
		userSession.setId(user.getId());
		userSession.setAnonUser(false);
		userSession.setUserToken(user.getToken());
		
		userSession.setUserRoles(Authority.getUserRoles(authorities));

			

		store(request, userSession);
		
		if (request.getAttribute(RequestKeys.REP.toString()) != null && ((Boolean)request.getAttribute(RequestKeys.REP.toString()))){
			Set<UserRole> roRoles = userSession.getUserRoles();
			Set<UserRole> roles = SetTool.createHashSet(roRoles);
			roles.add(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
		cacheInRequest(request, userSession);
		
	}
	
	/******************* tests **************************/
	
	public static class DatarouterSessionToolTests{
		@Test
		public void testSessionTokenLength(){
			String sessionToken = generateSessionToken();
			//expected base64 length: 256 bits / 6 bits/char => 42.667 => 43 chars
			Assert.assertEquals(43, sessionToken.length());
			
		}
	}
	
}
