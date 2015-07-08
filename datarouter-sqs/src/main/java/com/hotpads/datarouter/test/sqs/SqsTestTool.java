package com.hotpads.datarouter.test.sqs;

import org.testng.Assert;

import com.hotpads.util.core.bytes.StringByteTool;

public class SqsTestTool{

	public static String makeStringOfByteSize(int requiredSize){
		Assert.assertEquals(StringByteTool.getUtf8Bytes("a").length, 1);
		StringBuilder longString = new StringBuilder();
		for(int size = 0 ; size < requiredSize ; size++){
			longString.append("a");
		}
		return longString.toString();
	}
}
