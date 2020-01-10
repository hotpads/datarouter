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
package io.datarouter.client.memcached.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.memcached.client.DatarouterMemcachedKey;
import io.datarouter.client.memcached.tally.TallyKey;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;

public class DatarouterMemcachedKeyTests{

	@Test
	public void roundTrip(){
		String nodeName = "myClient.Tally";
		Integer databeanVersion = 1;
		TallyKey tallyKey = new TallyKey("one1566858501940!20190826152821");
		DatarouterMemcachedKey memcachedKey = new DatarouterMemcachedKey(nodeName, databeanVersion, tallyKey);

		String versionedKeyString = memcachedKey.getVersionedKeyString();
		String expected = DatarouterMemcachedKey.DATAROUTER_VERSION + ":" + nodeName + ":" + databeanVersion
				+ ":" + PrimaryKeyPercentCodec.encode(tallyKey);
		Assert.assertEquals(versionedKeyString, expected);

		DatarouterMemcachedKey decodedMemcachedKey = DatarouterMemcachedKey.parse(versionedKeyString, TallyKey.class);
		Assert.assertEquals(decodedMemcachedKey.nodeName, nodeName);
		Assert.assertEquals(decodedMemcachedKey.databeanVersion, databeanVersion);
		Assert.assertEquals(decodedMemcachedKey.primaryKey, tallyKey);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testNotEnough(){
		DatarouterMemcachedKey.parse("3:myClient.Tally:1one1566858501940%2120190826152821", null);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testTooMuch(){
		DatarouterMemcachedKey.parse("3:myClient.Tally:1:one15668:58501940%2120190826152821", null);
	}

}
