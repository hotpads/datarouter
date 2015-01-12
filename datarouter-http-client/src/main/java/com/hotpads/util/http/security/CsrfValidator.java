package com.hotpads.util.http.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsrfValidator{
	
	private static Logger logger = LoggerFactory.getLogger(CsrfValidator.class.getCanonicalName());
	private static final String HASHING_ALGORITHM = "SHA-256";
	private static final String MAIN_CIPHER_ALGORITHM = "AES";
	private static final String SUB_CIPHER_ALGORITHM = "CBC/PKCS5Padding";
	private static final String CIPHER_ALGORITHM = MAIN_CIPHER_ALGORITHM + "/" + SUB_CIPHER_ALGORITHM;
	private static final Long REQUEST_TIMEOUT_IN_MS = 10000L;

	private String cipherKey;
	private String cipherIv;
	
	public CsrfValidator(String cipherKey, String cipherIv){
		this.cipherIv = cipherIv;
		this.cipherKey = cipherKey;
	}

	public boolean check(String token){
		try{
			Cipher aes = getCipher(Cipher.DECRYPT_MODE);
			Long requestTime = Long.parseLong(new String(aes.doFinal(Base64.decodeBase64(token))));
			return System.currentTimeMillis() < requestTime + REQUEST_TIMEOUT_IN_MS;
		}catch (Exception e){
			log(e);
			return false;
		}
	}
	
	public String generateCsrfToken(){
		try{
			Cipher aes = getCipher(Cipher.ENCRYPT_MODE);
			return Base64.encodeBase64String(aes.doFinal(String.valueOf(System.currentTimeMillis()).getBytes()));
		}catch (Exception e){
			log(e);
			return null;
		}
	}

	private SecretKeySpec computeKey(String key) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);
		digest.update(cipherKey.getBytes());
		return new SecretKeySpec(digest.digest(), 0, 16, MAIN_CIPHER_ALGORITHM);
	}
	
	private void log(Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logger.warn(sw.toString());
	}
	
	private Cipher getCipher(int mode) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher aes = Cipher.getInstance(CIPHER_ALGORITHM);
		aes.init(mode, computeKey(cipherKey), new IvParameterSpec(cipherIv.getBytes(), 0, 16));
		return aes;
	}
	
}
