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
package io.datarouter.client.memcached.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.memcached.codec.MemcachedKey;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;

public class DatarouterMemcachedKeyTests{

	@Test
	public void roundTrip(){
		String nodeName = "myClient.Tally";
		int databeanVersion = 1;
		var tallyKey = new TallyKey("one1566858501940!20190826152821");

		String versionedKeyString = MemcachedKey.encode(nodeName, databeanVersion, tallyKey);
		String expected = MemcachedKey.CODEC_VERSION
				+ ":" + nodeName
				+ ":" + databeanVersion
				+ ":" + PrimaryKeyPercentCodecTool.encode(tallyKey);
		Assert.assertEquals(versionedKeyString, expected);

		var decodedMemcachedKey = MemcachedKey.decode(versionedKeyString, TallyKey.class);
		Assert.assertEquals(decodedMemcachedKey.nodeName, nodeName);
		Assert.assertEquals(decodedMemcachedKey.databeanVersion, databeanVersion);
		Assert.assertEquals(decodedMemcachedKey.primaryKey, tallyKey);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testNotEnough(){
		MemcachedKey.decode("3:myClient.Tally:1one1566858501940%2120190826152821", null);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testTooMuch(){
		MemcachedKey.decode("3:myClient.Tally:1:one15668:58501940%2120190826152821", null);
	}

}
