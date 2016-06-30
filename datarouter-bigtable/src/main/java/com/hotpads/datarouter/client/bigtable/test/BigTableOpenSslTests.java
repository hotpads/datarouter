package com.hotpads.datarouter.client.bigtable.test;

import org.apache.tomcat.jni.Library;
import org.apache.tomcat.jni.SSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.netty.handler.ssl.OpenSslEngine;
import io.netty.util.internal.NativeLibraryLoader;


public class BigTableOpenSslTests{
	private static final Logger logger = LoggerFactory.getLogger(BigTableOpenSslTests.class);

//	@Test
	public void testOpenSslAvailable(){
		io.netty.handler.ssl.OpenSsl.ensureAvailability();
	}

//	@Test
	public void testRepackagedOpenSslAvailable(){
		com.google.bigtable.repackaged.io.netty.handler.ssl.OpenSsl.ensureAvailability();
	}

//	@Test
	public void testManualLibraryLoad(){
        try {
            NativeLibraryLoader.load("netty-tcnative", SSL.class.getClassLoader());
            Library.initialize("provided");
            SSL.initialize(null);
        } catch (Throwable t) {
            logger.warn(
                    "Failed to load netty-tcnative; " +
                            OpenSslEngine.class.getSimpleName() + " will be unavailable.", t);
        }
	}

	@Test
	public void testNettyTcnative(){
        NativeLibraryLoader.load("netty-tcnative", SSL.class.getClassLoader());
	}
}
