package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.Assert;

import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.ThreadTool;

public class SqsTestHelper{

	private QueueStorageWriter<TestDatabeanKey,TestDatabean> queueStorageWriter;

	public SqsTestHelper(QueueStorageWriter<TestDatabeanKey,TestDatabean> queueStorageWriter){
		this.queueStorageWriter = queueStorageWriter;
	}

	public void testByteLimitMulti(){
		String longString = makeLongStringWithDatabeanSizeTarget(BaseSqsNode.MAX_BYTES_PER_MESSAGE + 1);
		List<TestDatabean> databeans = new ArrayList<>();
		databeans.add(new TestDatabean(longString, "", ""));
		databeans.add(new TestDatabean(longString, "", ""));
		databeans.add(new TestDatabean("demat", "", ""));
		try{
			queueStorageWriter.putMulti(databeans, null);
		}catch(SqsDataTooLargeException exception){
			Assert.assertEquals(exception.getRejectedDatabeans().size(), 2);
		}
	}

	public static void testInterruptPeek(Callable<Void> longPeekCallable){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		long start = System.currentTimeMillis();
		Future<Void> future = executor.submit(longPeekCallable);
		ThreadTool.sleep(1000);
		future.cancel(true);
		executor.shutdown();
		Assert.assertTrue(System.currentTimeMillis() - start < 5000);
	}

	public static String makeStringOfByteSize(int requiredSize){
		Assert.assertEquals(StringByteTool.getUtf8Bytes("a").length, 1);
		StringBuilder longString = new StringBuilder();
		for(int size = 0 ; size < requiredSize ; size++){
			longString.append("a");
		}
		return longString.toString();
	}

	public static String makeLongStringWithDatabeanSizeTarget(int size){
		TestDatabean emptyDatabean = new TestDatabean("", "", "");
		TestDatabeanFielder fielder = new TestDatabeanFielder();
		String stringDatabean = fielder.getStringDatabeanCodec().toString(emptyDatabean, fielder);
		int emptyDatabeanSize = StringByteTool.getUtf8Bytes(stringDatabean).length;
		return SqsTestHelper.makeStringOfByteSize(size - emptyDatabeanSize);
	}
}
