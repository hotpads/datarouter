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
package io.datarouter.web.user.authenticate;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import io.datarouter.util.bytes.StringByteTool;

public class DatarouterTokenGenerator{

	public static String generateRandomToken(){
		SecureRandom secureRandom;
		try{
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException("misconfigured - no secure random");
		}
		byte[] sha1Bytes = new byte[32];
		secureRandom.nextBytes(sha1Bytes);
		byte[] sha256Bytes = DigestUtils.sha256(sha1Bytes);//further encode older sha1
		byte[] base64Bytes = Base64.encodeBase64URLSafe(sha256Bytes);
		String randomString = StringByteTool.fromUtf8Bytes(base64Bytes);
		return randomString;
	}

}
