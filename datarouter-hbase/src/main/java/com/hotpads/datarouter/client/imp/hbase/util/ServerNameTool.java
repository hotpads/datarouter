package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.hbase.ServerName;
import org.testng.annotations.Test;

import com.hotpads.util.core.java.ReflectionTool;

public class ServerNameTool{

	public static ServerName create(final String hostname, final int port, final long startcode){
		Collection<?> constructorParams = Arrays.asList(hostname, port, startcode);
		return ReflectionTool.createWithParameters(ServerName.class, constructorParams);
	}


	/******************* Tests *****************************/

	public static class ServerNameToolTests{
		@Test
		public void testCreateViaReflection(){
			create("theHostname", 12345, 9876543);
		}
	}

}
