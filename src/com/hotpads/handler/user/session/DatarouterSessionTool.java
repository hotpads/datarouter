package com.hotpads.handler.user.session;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.hotpads.handler.CookieTool;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.websupport.search.SearchConstants;

public class DatarouterSessionTool{
	
	/************* static fields **********************/

	private final static String REQUEST_ATTRIBUTE_NAME = "userSession";
	private static final int 
		SESSION_TOKEN_COOKIE_EXPIRATION_SECONDS = 30 * 60,
		USER_TOKEN_COOKIE_EXPIRATION_SECONDS = 365 * 24 * 3600;
	
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
	
	public static void addSessionTokenCookie(HttpServletResponse response, String sessionToken){
		CookieTool.addCookie(response, DatarouterCookieKeys.sessionToken.toString(), sessionToken, "/", 
				SESSION_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static void addUserTokenCookie(HttpServletResponse response, String userToken){
		CookieTool.addCookie(response, DatarouterCookieKeys.userToken.toString(), userToken, "/", 
				USER_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, DatarouterCookieKeys.sessionToken.toString());
	}

	public static DatarouterSession getFromRequest(HttpServletRequest request) {
		return (DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
	}
	
	public static void addToRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession); 
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
