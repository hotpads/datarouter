package com.hotpads.datarouter.test;

import com.hotpads.datarouter.client.ClientId;



public class DrTestConstants{

	public static final String
		CONFIG_PATH = "/hotpads/config/datarouter-test.properties",
		DATABASE_drTest0 = "drTest0";
	public static final ClientId
		CLIENT_drTestMemory = new ClientId("memory0", true),
		CLIENT_drTestJdbc0 = new ClientId("drTestJdbc0", true),
		CLIENT_drTestHibernate0 = new ClientId("drTestHibernate0", true),
		CLIENT_drTestHBase = new ClientId("drTestHBase", true),
		CLIENT_drTestMemcached = new ClientId("drTestMemcached", true),
		CLIENT_drTestSqs = new ClientId("drTestSqs", true);

}
