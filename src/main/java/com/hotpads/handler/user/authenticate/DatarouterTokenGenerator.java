package com.hotpads.handler.user.authenticate;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.hotpads.util.core.bytes.StringByteTool;

public class DatarouterTokenGenerator{
	
	public static String generateRandomToken(){
		SecureRandom secureRandom;
		try{
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("misconfigured - no secure random");
		}
		byte[] sha1Bytes = new byte[32];
		secureRandom.nextBytes(sha1Bytes);
		byte[] sha256Bytes = DigestUtils.sha256(sha1Bytes);//further encode older sha1
		byte[] base64Bytes = Base64.encodeBase64URLSafe(sha256Bytes);
		String randomString = StringByteTool.fromUtf8Bytes(base64Bytes);
		return randomString;
	}

	
	/******************* tests **************************/
	
	public static class DatarouterSessionToolTests{
		@Test
		public void testSessionTokenLength(){
			String sessionToken = generateRandomToken();
			//expected base64 length: 256 bits / 6 bits/char => 42.667 => 43 chars
			Assert.assertEquals(43, sessionToken.length());
			
		}
	}
}
