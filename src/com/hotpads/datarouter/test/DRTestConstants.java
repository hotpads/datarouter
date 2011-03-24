package com.hotpads.datarouter.test;

import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.util.core.ListTool;

public class DRTestConstants{

	public static final String
		CONFIG_PATH = "/hotpads/config/datarouter-test.properties",
		CLIENT_drTestHibernate0 = "drTestHibernate0",
		CLIENT_drTestHBase = "drTestHBase",
		CLIENT_drTestMemcached = "drTestMemcached";

	public static List<ClientType> clientTypes = ListTool.create();
	public static List<Object[]> clientTypeObjectArrays = ListTool.create();
	static{
		clientTypes.add(ClientType.hibernate);
		clientTypes.add(ClientType.hbase);
		clientTypes.add(ClientType.memcached);
		for(ClientType clientType : clientTypes){
			clientTypeObjectArrays.add(new Object[]{clientType});
		}
	}
}
