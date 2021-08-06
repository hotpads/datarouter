/*
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
import java.util.Base64.Encoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
 */
public class PasswordTool{
	private static final Logger logger = LoggerFactory.getLogger(PasswordTool.class);

	private static final int NUM_DIGEST_ITERATIONS = 2471;
	// no padding for consistency with previously used org.apache.commons.codec.binary.Base64.encodeBase64URLSafe
	private static final Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final SecureRandom SECURE_RANDOM = initRandom();

	private static SecureRandom initRandom(){
		try{
			return SecureRandom.getInstance("SHA1PRNG", "SUN");
		}catch(NoSuchAlgorithmException | NoSuchProviderException e){
			throw new RuntimeException(e);
		}
	}

	public static String digest(String salt, String rawPassword){
		String sr = salt + rawPassword;
		for(int i = 0; i < NUM_DIGEST_ITERATIONS; ++i){
			sr = DigestUtils.sha256Hex(sr);
		}
		return sr;
	}

	/**
	 * this can be slow, call only if necessary
	 */
	public static String generateSalt(){
		byte[] salt = new byte[16];
		long start = System.nanoTime();
		SECURE_RANDOM.nextBytes(salt);
		long durationNs = System.nanoTime() - start;
		if(durationNs > 1_000_000){
			logger.warn("slow random number generation durationNs={}", durationNs);
		}
		return BASE64_ENCODER.encodeToString(salt);
	}

}
