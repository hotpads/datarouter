package com.hotpads.util.http.client;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;

public class StrictHostnameOrHotpadsVerifier extends AbstractVerifier{

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
		if(!host.equals("hotpads.com") || !host.endsWith(".hotpads.com")){
			verify(host, cns, subjectAlts, true);
		}
	}
	
	@Override
    public final String toString() {
        return "STRICT_OR_HOTPADS";
    }
	
}