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
package io.datarouter.aws.sqs.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.bytes.StringByteTool;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.util.concurrent.ThreadTool;

public class SqsTestHelper{

	private final SqsTestHelperDao dao;

	public SqsTestHelper(SqsTestHelperDao dao){
		this.dao = dao;
	}

	public void testByteLimitMulti(){
		String longString = makeLongStringWithDatabeanSizeTarget(BaseSqsNode.MAX_BYTES_PER_MESSAGE + 1);
		List<TestDatabean> databeans = new ArrayList<>();
		databeans.add(new TestDatabean(longString, "", ""));
		databeans.add(new TestDatabean(longString, "", ""));
		databeans.add(new TestDatabean("demat", "", ""));
		try{
			dao.putMulti(databeans);
		}catch(SqsDataTooLargeException exception){
			Assert.assertEquals(exception.getRejectedDatabeans().size(), 2);
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
		Assert.assertEquals(StringByteTool.getUtf8Bytes("a").length, 1);
		var longString = new StringBuilder();
		for(int size = 0; size < requiredSize; size++){
			longString.append("a");
		}
		return longString.toString();
	}

	public static String makeLongStringWithDatabeanSizeTarget(int size){
		var emptyDatabean = new TestDatabean("", "", "");
		var fielder = new TestDatabeanFielder();
		String stringDatabean = fielder.getStringDatabeanCodec().toString(emptyDatabean, fielder);
		int emptyDatabeanSize = StringByteTool.getUtf8Bytes(stringDatabean).length;
		return SqsTestHelper.makeStringOfByteSize(size - emptyDatabeanSize);
	}

}
