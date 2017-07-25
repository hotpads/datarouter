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
package io.datarouter.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.bytes.StringByteTool;

public class HashMethods{

	public static long longDjbHash(String str){
		long hash = 5381L;
		for(int i = 0; i < str.length(); i++){
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		return hash & 0x7FFFFFFFFFFFFFFFL;
	}

	public static long longDjbHash(byte[] in){
		long hash = 5381L;
		for(int i = 0; i < in.length; i++){
			hash = ((hash << 5) + hash) + in[i];
		}
		return hash & 0x7FFFFFFFFFFFFFFFL;
	}

	public static Long longMd5DjbHash(String in){
		return longMd5DjbHash(StringByteTool.getUtf8Bytes(in));
	}

	public static Long longMd5DjbHash(byte[] in){
		MessageDigest md5 = md5MessageDigest();
		md5.update(in);
		return longDjbHash(md5.digest());
	}

	public static MessageDigest md5MessageDigest(){
		try{
			return MessageDigest.getInstance("MD5");
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException("MD5 not defined", e);
		}
	}

	public static class DrHashMethodsTests{
		@Test
		public void testLongDjb(){
			long hash1 = longDjbHash("public-school_HOLMES ELEMENTARY_4902 MT. ARARAT DR_SAN DIEGO_CA_92111");
			long hash2 = longDjbHash("private-school_Burleson Adventist School_1635 Fox Lane_Burleson_TX_76028");
			Assert.assertFalse(hash1 == hash2);
		}

		@Test
		public void testMd5(){
			Set<Long> buckets = new TreeSet<>();
			for(int serverNum = 98; serverNum <= 101; ++serverNum){
				String serverName = "HadoopNode98:10012:" + serverNum;
				for(int i = 0; i < 1000; ++i){
					Long bucket = longMd5DjbHash(StringByteTool.getUtf8Bytes(serverName + i));
					buckets.add(bucket);
				}
			}
			int counter = 0;
			double avg = 0;
			for(Long b : buckets){
				avg = (avg * counter + b) / (counter + 1);
				++counter;
			}
		}
	}
}
