package com.hotpads.util.http.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CsrfValidator{
	private static final String HASHING_ALGORITHM = "SHA-256";
	// AES/CBC requires IV to be generated for every encrypted message!!
	// More details here: https://tools.ietf.org/html/rfc3602
	// The Encapsulating Security Payload (ESP) payload is made up of the IV and the raw cipher-text.
	// The IV field MUST be the same size as the block size of the cipher algorithm being used.
	// The IV MUST be chosen at random, and MUST be unpredictable.
	private static final String MAIN_CIPHER_ALGORITHM = "AES";
	private static final String SUB_CIPHER_ALGORITHM = "CBC/PKCS5Padding";
	private static final String CIPHER_ALGORITHM = MAIN_CIPHER_ALGORITHM + "/" + SUB_CIPHER_ALGORITHM;
	private static final Long REQUEST_TIMEOUT_IN_MS = 10000L;

	private final String cipherKey;

	public CsrfValidator(String cipherKey){
		this.cipherKey = cipherKey;
	}

	public static String generateCsrfIv(){
		SecureRandom sr;
		try{
			sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
		}catch(NoSuchAlgorithmException | NoSuchProviderException e){
			throw new RuntimeException("error in SecureRandom.getInstance()");
		}
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public boolean check(String token, String cipherIv){
		Long requestTime = getRequestTimeMs(token, cipherIv);
		if(requestTime == null){
			return false;
		}
		return System.currentTimeMillis() < requestTime + REQUEST_TIMEOUT_IN_MS;
	}

	public String generateCsrfToken(String cipherIv){
		try{
			Cipher aes = getCipher(Cipher.ENCRYPT_MODE, cipherIv);
			return Base64.getEncoder().encodeToString(aes.doFinal(String.valueOf(System.currentTimeMillis())
					.getBytes()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public Long getRequestTimeMs(String token, String cipherIv){
		try{
			Cipher aes = getCipher(Cipher.DECRYPT_MODE, cipherIv);
			return Long.parseLong(new String(aes.doFinal(Base64.getDecoder().decode(token))));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private SecretKeySpec computeKey(String cipherKey) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);
		digest.update(cipherKey.getBytes());
		return new SecretKeySpec(digest.digest(), 0, 16, MAIN_CIPHER_ALGORITHM);
	}

	private Cipher getCipher(int mode, String cipherIv)
	throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher aes = Cipher.getInstance(CIPHER_ALGORITHM);
		aes.init(mode, computeKey(cipherKey), new IvParameterSpec(cipherIv.getBytes(), 0, 16));
		return aes;
	}

}
