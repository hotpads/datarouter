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
package io.datarouter.web.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HttpSignatureTool{

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	/**
	 * Generates a HMAC-SHA1 signature using the specified secretKey.
	 */
	public static String generateSignature(String endPoint, Map<String,String> params, String secretKey){
		StringBuilder result = new StringBuilder();
		try{
			for(Entry<String,String> entry : params.entrySet()){
				result.append(entry.getKey()).append('=').append(
						URLEncoder.encode(entry.getValue(), "UTF-8").replaceAll("\\+", "%20")).append('&');
			}
			String url = endPoint + '?' + result.deleteCharAt(result.length() - 1).toString();
			// get a hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM);
			// get a hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// compute hmac on input data bytes
			byte[] rawHmac = mac.doFinal(url.getBytes());
			// base64-encode hmac and trim off /r/n
			return Base64.encodeBase64String(rawHmac).trim();
		}catch(InvalidKeyException e){
			throw new IllegalArgumentException(e);
		}catch(NoSuchAlgorithmException e){
			throw new IllegalArgumentException(e);
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException(e);
		}
	}

}
