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
package io.datarouter.web.util;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PasswordTool{

	/*
	 * http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
	 */

	private static final int NUM_DIGEST_ITERATIONS = 2471;

	public static String digest(String salt, String rawPassword){
		String sr = salt + rawPassword;
		for(int i = 0; i < NUM_DIGEST_ITERATIONS; ++i){
			sr = DigestUtils.sha256Hex(sr);
		}
		return sr;
	}

	public static String generateSalt(){
		SecureRandom sr;
		try{
			sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
		}catch(NoSuchAlgorithmException | NoSuchProviderException e){
			throw new RuntimeException(e);
		}
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		// no padding for consistency with previously used org.apache.commons.codec.binary.Base64.encodeBase64URLSafe
		return Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
	}

	public static class PasswordToolTests{

		@Test
		public void testDigest(){
			long startNs = System.nanoTime();
			digest(System.currentTimeMillis() + "", "IrregularAustralia56");
			long elapsedNs = System.nanoTime() - startNs;
			Assert.assertTrue(elapsedNs < 300 * 1000 * 1000);// less than 300ms (taking 81ms in testing)
		}

	}

}
