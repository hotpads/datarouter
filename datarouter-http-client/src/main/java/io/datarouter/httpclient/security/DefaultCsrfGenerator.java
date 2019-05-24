/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.httpclient.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DefaultCsrfGenerator implements CsrfGenerator{

	private static final String HASHING_ALGORITHM = "SHA-256";

	// AES/CBC requires IV to be generated for every encrypted message!!
	// More details here: https://tools.ietf.org/html/rfc3602
	// The Encapsulating Security Payload (ESP) payload is made up of the IV and the raw cipher-text.
	// The IV field MUST be the same size as the block size of the cipher algorithm being used.
	// The IV MUST be chosen at random, and MUST be unpredictable.
	private static final String MAIN_CIPHER_ALGORITHM = "AES";
	private static final String SUB_CIPHER_ALGORITHM = "CBC/PKCS5Padding";
	private static final String CIPHER_ALGORITHM = MAIN_CIPHER_ALGORITHM + "/" + SUB_CIPHER_ALGORITHM;

	private final Supplier<String> cipherKeySupplier;

	public DefaultCsrfGenerator(Supplier<String> cipherKeySupplier){
		this.cipherKeySupplier = cipherKeySupplier;
	}

	@Override
	public String generateCsrfToken(String cipherIv){
		try{
			Cipher aes = getCipher(Cipher.ENCRYPT_MODE, cipherIv);
			return Base64.getEncoder().encodeToString(aes.doFinal(String.valueOf(System.currentTimeMillis())
					.getBytes()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public Cipher getCipher(int mode, String cipherIv)
	throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
		Cipher aes = Cipher.getInstance(CIPHER_ALGORITHM);
		aes.init(mode, computeKey(cipherKeySupplier.get()), new IvParameterSpec(cipherIv.getBytes(), 0, 16));
		return aes;
	}

	private SecretKeySpec computeKey(String cipherKey) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);
		digest.update(cipherKey.getBytes());
		return new SecretKeySpec(digest.digest(), 0, 16, MAIN_CIPHER_ALGORITHM);
	}

	@Override
	public String generateCsrfIv(){
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

}
