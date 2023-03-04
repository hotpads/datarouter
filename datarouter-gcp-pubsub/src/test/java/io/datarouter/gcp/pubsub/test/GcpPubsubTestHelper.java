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
package io.datarouter.gcp.pubsub.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.util.concurrent.ThreadTool;

public class GcpPubsubTestHelper{

	private final GcpPubsubTestHelperDao dao;

	public GcpPubsubTestHelper(GcpPubsubTestHelperDao dao){
		this.dao = dao;
	}

	public void testByteLimitMulti(){
		String longString = makeLongStringWithDatabeanSizeTarget(BaseGcpPubsubNode.MAX_SERIALIZED_REQUEST_SIZE);
		String okString = makeLongStringWithDatabeanSizeTarget(
				BaseGcpPubsubNode.MAX_TOPIC_PLUS_MESSAGE_SIZE - dao.getTopicLength());
		List<TestDatabean> databeans = new ArrayList<>();
		databeans.add(new TestDatabean(longString, "", ""));
		databeans.add(new TestDatabean(okString, "", ""));
		databeans.add(new TestDatabean("hello", "", ""));
		try{
			dao.putMulti(databeans);
		}catch(GcpPubsubDataTooLargeException exception){
			Assert.assertEquals(exception.getRejectedDatabeans().size(), 1);
		}
	}

	public static void testInterruptPeek(Callable<Void> longPeekCallable){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		long start = System.currentTimeMillis();
		Future<Void> future = executor.submit(longPeekCallable);
		ThreadTool.sleepUnchecked(1000);
		future.cancel(true);
		executor.shutdown();
		Assert.assertTrue(System.currentTimeMillis() - start < 5000);
	}

	public static String makeStringOfByteSize(int requiredSize){
		Assert.assertEquals(StringCodec.UTF_8.encode("a").length, 1);
		return "a".repeat(requiredSize);
	}

	public static String makeLongStringWithDatabeanSizeTarget(int size){
		var emptyDatabean = new TestDatabean("", "", "");
		var fielder = new TestDatabeanFielder();
		String stringDatabean = fielder.getStringDatabeanCodec().toString(emptyDatabean, fielder);
		int emptyDatabeanSize = StringCodec.UTF_8.encode(stringDatabean).length;
		return GcpPubsubTestHelper.makeStringOfByteSize(size - emptyDatabeanSize);
	}

}
