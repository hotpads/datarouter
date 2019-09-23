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

import java.util.Set;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.bytes.StringByteTool;

public class HashMethodsTests{

	@Test
	public void testLongDjb(){
		long hash1 = HashMethods.longDjbHash("public-school_HOLMES ELEMENTARY_4902 MT. ARARAT DR_SAN DIEGO_CA_92111");
		long hash2 = HashMethods.longDjbHash(
				"private-school_Burleson Adventist School_1635 Fox Lane_Burleson_TX_76028");
		Assert.assertFalse(hash1 == hash2);
	}

	@Test
	public void testMd5DjbHash(){
		Set<Long> buckets = new TreeSet<>();
		for(int serverNum = 98; serverNum <= 101; ++serverNum){
			String serverName = "HadoopNode98:10012:" + serverNum;
			for(int i = 0; i < 1000; ++i){
				Long bucket = HashMethods.longMd5DjbHash(StringByteTool.getUtf8Bytes(serverName + i));
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

	@Test
	public void testMd5Hash(){
		String hash = HashMethods.md5Hash("hello world!");
		Assert.assertEquals(hash, "fc3ff98e8c6a0d3087d515c0473f8677");
	}

}
