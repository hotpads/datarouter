package com.hotpads.datarouter.test;

import com.hotpads.datarouter.client.ClientId;


public class DatarouterTestClientIds{

	public static final ClientId
			memory = new ClientId("memory0", true),
			jdbc0 = new ClientId("drTestJdbc0", true),
			hbase = new ClientId("drTestHBase", true),
			bigTable = new ClientId("drTestBigTable", true),
			memcached = new ClientId("drTestMemcached", true),
			redis = new ClientId("drTestRedis", true),
			CLIENT_drTestSqs = new ClientId("drTestSqs", true),
			CLIENT_drTestKinesis = new ClientId("drTestKinesis", true);
}