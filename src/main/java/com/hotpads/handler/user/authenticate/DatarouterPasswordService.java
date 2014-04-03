package com.hotpads.handler.user.authenticate;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.bytes.StringByteTool;

/*
 * http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
 */
public class DatarouterPasswordService{
	
	private static final int NUM_DIGEST_ITERATIONS = 2471;

	@Inject
	private DatarouterUserNodes userNodes;
	
	
	public String digest(String salt, String rawPassword){
		String s = salt + rawPassword;
		for(int i=0; i < NUM_DIGEST_ITERATIONS; ++i){
			s = DigestUtils.sha256Hex(s);
		}
		return s;
	}
	
	public boolean isPasswordCorrect(DatarouterUser user, String rawPassword){
		if(user==null || rawPassword==null){ return false; }
		String passwordDigest = digest(user.getPasswordSalt(), rawPassword);
		return ObjectTool.equals(user.getPasswordDigest(), passwordDigest);
	}
	
	public boolean isPasswordCorrect(String email, String rawPassword){
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUsernameLookup(email), null);
		return isPasswordCorrect(user, rawPassword);
	}
	
	public String generateSaltForNewUser(){
        SecureRandom sr;
		try{
			sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
		}catch(NoSuchAlgorithmException | NoSuchProviderException e){
			throw new RuntimeException("error in SecureRandom.getInstance()");
		}
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        byte[] base64Salt = Base64.encodeBase64URLSafe(salt);
        return StringByteTool.fromUtf8Bytes(base64Salt);
	}
	
	public void updateUserPassword(DatarouterUser user, String password) {
		String passwordSalt = generateSaltForNewUser();
		String passwordDigest = digest(passwordSalt, password);
		user.setPasswordSalt(passwordSalt);
		user.setPasswordDigest(passwordDigest);
		userNodes.getUserNode().put(user, null);
	}
	
	
	/******************* tests ***********************/
	
	public static class ReputationPasswordServiceTests{
		@Test
		public void testDigest(){
			long startNs = System.nanoTime();
			new DatarouterPasswordService().digest(System.currentTimeMillis()+"", "IrregularAustralia56");
			long elapsedNs = System.nanoTime() - startNs;
			System.out.println(elapsedNs);
			Assert.assertTrue(elapsedNs < 300*1000*1000);//less than 300ms (taking 81ms in testing)
		}
	}
}
