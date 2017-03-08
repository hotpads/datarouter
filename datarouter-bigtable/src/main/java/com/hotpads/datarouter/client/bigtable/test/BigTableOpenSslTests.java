package com.hotpads.datarouter.client.bigtable.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.cloud.bigtable.grpc.BigtableSession;

import io.netty.handler.ssl.OpenSsl;

public class BigTableOpenSslTests{

	@Test
	public void testOpenSslAvailable(){
		if(!BigtableSession.isAlpnProviderEnabled()){
			if(OpenSsl.isAvailable()){
				// Should not happen
				Assert.fail("big table alpn provider is not enable even if open ssl is available");
			}else{
				Assert.fail("open ssl is not available because: " + OpenSsl.unavailabilityCause());
			}
		}
	}

}
