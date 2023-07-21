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
package io.datarouter.bytes.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum DigestAlgorithm{
	MD5("MD5"),
	SHA_256("SHA-256"),
	;

	private final String algorithm;

	DigestAlgorithm(String algorithm){
		this.algorithm = algorithm;
	}

	public MessageDigest getMessageDigest(){
		try{
			return MessageDigest.getInstance(algorithm);
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}

}
